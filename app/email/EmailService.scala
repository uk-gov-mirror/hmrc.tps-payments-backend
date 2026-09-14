/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package email

import email.model.{EmailSendRequest, IndividualPaymentForEmail}
import play.api.Logger
import play.api.libs.json.JsArray
import play.api.libs.json.Json.toJson
import tps.journey.model.Journey
import tps.model.TaxTypes.{MIB, PNGR}
import tps.model.{Email, PaymentItem, TaxType, TaxTypes}
import tps.pcipalmodel.{ChargeRefNotificationPcipalRequest, StatusTypes}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class EmailService @Inject() (emailConnector: EmailConnector)(using ec: ExecutionContext) {

  /** This function sends email: if all notifications has been received (one for each PaymentItem) and if the tax type is not PNGR nor MIB and if the email
    * address has been provided and if at least one payment has succeeded (failed payments aren't aggregated into the email)
    *
    * (!) Since the each PaymentItem can have one email address and only one aggregate email is sent the first found email from the list of PaymentItems is
    * taken (!!) The referenceNumber being part of the emails (randomly generated number without two last characters) from first paymentItem found on the list
    * is used as a reference for the client presented in the email)
    *
    * (this function had been developed before this scaladoc)
    */
  def maybeSendEmail(journey: Journey)(using hc: HeaderCarrier): Unit = {
    val paymentItems: List[PaymentItem] = journey.payments
    if weShouldSendEmail(paymentItems) then
      val emailAddress: Email                                = paymentItems
        .find(_.email.nonEmpty)
        .flatMap(_.email)
        .getOrElse(throw new RuntimeException("Missing email in payment items"))
      val listOfSuccessfulTpsPaymentItems: List[PaymentItem] =
        paymentItems.filter(
          _.pcipalData
            .fold(throw new RuntimeException("maybeSendEmail error: pcipal data should be present but isn't"))(nextPaymentItemPciPalData =>
              nextPaymentItemPciPalData.Status == StatusTypes.validated
            )
        )

      listOfSuccessfulTpsPaymentItems.headOption match {
        case Some(
              PaymentItem(
                _,
                _,
                _,
                _,
                _,
                _,
                Some(
                  ChargeRefNotificationPcipalRequest(_, _, _, _, cardType, _, _, _, _, _, referenceNumber, cardLast4)
                ),
                _,
                _,
                _,
                receiptInWelsh,
                _
              )
            ) =>
          sendEmail(
            payments = listOfSuccessfulTpsPaymentItems,
            transactionReference = referenceNumber.dropRight(2),
            emailAddress = emailAddress,
            cardType = cardType,
            cardNumber = cardLast4,
            receiptInWelsh = receiptInWelsh
          )
        case _ => ()
      }
    else ()
  }

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  private def sendEmail(
    payments:             List[PaymentItem],
    transactionReference: String,
    emailAddress:         Email,
    cardType:             String,
    cardNumber:           String,
    receiptInWelsh:       Boolean
  )(using hc: HeaderCarrier): Unit = {

    val totalCommissionPaid: BigDecimal = payments
      .map(nextTpsPaymentItem => nextTpsPaymentItem.pcipalData.fold(BigDecimal(0))(pcipalData => pcipalData.Commission))
      .sum

    val totalAmountPaid: BigDecimal = payments.map(nextTpsPaymentItem => nextTpsPaymentItem.amount).sum

    val emailSendRequest: EmailSendRequest = EmailSendRequest(
      to = Seq(emailAddress),
      templateId = if receiptInWelsh then "telephone_payments_service_cy" else "telephone_payments_service",
      parameters = Map[String, String](
        "transactionReference"    -> transactionReference,
        "totalAmountPaid"         -> parseBigDecimalToString(totalCommissionPaid + totalAmountPaid),
        "cardType"                -> cardType,
        "cardNumber"              -> cardNumber,
        "tpsPaymentItemsForEmail" -> stringifyTpsPaymentsItemsForEmail(payments.map(toIndividualPaymentForEmail))
      )
    )

    emailConnector
      .sendEmail(emailSendRequest)
      .recover { case e =>
        logger.error("Failed to send email, investigate", e)
      }
  }

  private def weShouldSendEmail(tpsPaymentItems: List[PaymentItem]): Boolean =
    isNotMibOrPngr(tpsPaymentItems) && tpsPaymentsAreFullyUpdated(tpsPaymentItems) && emailAddressHasBeenProvided(
      tpsPaymentItems
    )

  private def tpsPaymentsAreFullyUpdated(tpsPaymentItems: List[PaymentItem]): Boolean =
    tpsPaymentItems.forall(_.pcipalData.nonEmpty)

  private def emailAddressHasBeenProvided(tpsPaymentItems: List[PaymentItem]): Boolean =
    tpsPaymentItems.exists(_.email.nonEmpty)

  private def isNotMibOrPngr(tpsPaymentItems: List[PaymentItem]): Boolean =
    !tpsPaymentItems.exists(nextPaymentItem => nextPaymentItem.taxType == MIB || nextPaymentItem.taxType == PNGR)

  def toIndividualPaymentForEmail(paymentItem: PaymentItem): IndividualPaymentForEmail = IndividualPaymentForEmail(
    taxType = getTaxTypeString(paymentItem.taxType),
    amount = parseBigDecimalToString(paymentItem.amount),
    // TODO: at this stage the pciPalData should be always there, right? If so then it should not bother with "Unknown"
    transactionFee = paymentItem.pcipalData.fold("Unknown")(pcipalData => parseBigDecimalToString(pcipalData.Commission)),
    transactionNumber = paymentItem.pcipalData.fold("Unknown")(pcipalData => pcipalData.ReferenceNumber)
  )

  def stringifyTpsPaymentsItemsForEmail(tpsPaymentsForEmail: List[IndividualPaymentForEmail]): String = JsArray(
    tpsPaymentsForEmail.map(toJson(_))
  ).toString

  private def parseBigDecimalToString(bigDecimal: BigDecimal): String = bigDecimal.setScale(2).toString

  private def getTaxTypeString(taxType: TaxType): String = taxType match {
    case TaxTypes.ChildBenefitsRepayments => "Child Benefits repayments"
    case TaxTypes.Sa                      => "Self Assessment"
    case TaxTypes.Sdlt                    => "Stamp Duty Land Tax"
    case TaxTypes.Safe                    => "SAFE"
    case TaxTypes.Cotax                   => "Corporation Tax"
    case TaxTypes.Ntc                     => "Tax credit repayments"
    case TaxTypes.Paye                    => "PAYE"
    case TaxTypes.Nps                     => "NPS/NIRS"
    case TaxTypes.Vat                     => "VAT"
    case TaxTypes.VatIossAndOss           => "VAT IOSS and OSS"
    case TaxTypes.Ppt                     => "Plastic Packaging Tax"
    case TaxTypes.MIB                     => taxType.toString
    case TaxTypes.PNGR                    => taxType.toString
  }

  private lazy val logger = Logger(this.getClass)
}
