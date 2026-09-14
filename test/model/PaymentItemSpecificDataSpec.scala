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

package model

import play.api.libs.json.{JsResultException, JsString}
import testsupport.UnitSpec
import tps.model.*
import tps.model.vatiossandoss.CalendarQuarter.AprilToJune
import tps.model.vatiossandoss.{CalendarPeriod, CalendarQuarterlyPeriod, IossAndOssPaymentTypes, IossOssChargeReference, IossRegistrationNumber, OssVrn}

class PaymentItemSpecificDataSpec extends UnitSpec {

  "PaymentSpecificData" - {

    val testReference = "someReference"

    "PngrSpecificData" - {

      val testPngrSpecificData = PngrSpecificData(testReference)

      "getReference should return the chargeReference" in {
        testPngrSpecificData.getReference shouldBe testReference
      }

      "getRawReference should return the chargeReference" in {
        testPngrSpecificData.getRawReference shouldBe testReference
      }

      "searchTag should return the chargeReference" in {
        testPngrSpecificData.searchTag shouldBe testReference
      }
    }

    "MibSpecificData" - {

      val testMibSpecificData = MibSpecificData(testReference, BigDecimal(1), BigDecimal(1), Some(1))

      "getReference should return the chargeReference" in {
        testMibSpecificData.getReference shouldBe testReference
      }

      "getRawReference should return the chargeReference" in {
        testMibSpecificData.getRawReference shouldBe testReference
      }

      "searchTag should return the chargeReference" in {
        testMibSpecificData.searchTag shouldBe testReference
      }

      "getAmendmentReference should return the amendmentReference" in {
        testMibSpecificData.getAmendmentReference shouldBe Some(1)
      }
    }

    "ChildBenefitSpecificData" - {

      val testChildBenefitSpecificData = ChildBenefitSpecificData(testReference)

      "getReference should return the childBenefitYReference" in {
        testChildBenefitSpecificData.getReference shouldBe testReference
      }

      "getRawReference should return the childBenefitYReference" in {
        testChildBenefitSpecificData.getRawReference shouldBe testReference
      }

      "searchTag should return the childBenefitYReference" in {
        testChildBenefitSpecificData.searchTag shouldBe testReference
      }
    }

    "SaSpecificData" - {

      val testSaSpecificData = SaSpecificData("1234567895K")

      "getReference should return the saReference as is" in {
        testSaSpecificData.getReference shouldBe "1234567895K"
      }

      "getRawReference should return the saReference with last character dropped" in {
        testSaSpecificData.getRawReference shouldBe "1234567895"
      }

      "searchTag should return the saReference" - {
        "without any K when there is one" in {
          testSaSpecificData.searchTag shouldBe "1234567895"
        }
        "without any K when it's randomly in the middle of the string" in {
          SaSpecificData("123K4567895").searchTag shouldBe "1234567895"
        }
        "without any K when it's randomly at the start of the string" in {
          SaSpecificData("K1234567895").searchTag shouldBe "1234567895"
        }
      }
    }

    "SdltSpecificData" - {

      val testSdltSpecificData = SdltSpecificData(testReference)

      "getReference should return the sdltReference" in {
        testSdltSpecificData.getReference shouldBe testReference
      }

      "getRawReference should return the sdltReference" in {
        testSdltSpecificData.getRawReference shouldBe testReference
      }

      "searchTag should return the sdltReference" in {
        testSdltSpecificData.searchTag shouldBe testReference
      }
    }

    "SafeSpecificData" - {

      val testSafeSpecificData = SafeSpecificData(testReference)

      "getReference should return the safeReference" in {
        testSafeSpecificData.getReference shouldBe testReference
      }

      "getRawReference should return the safeReference" in {
        testSafeSpecificData.getRawReference shouldBe testReference
      }

      "searchTag should return the safeReference" in {
        testSafeSpecificData.searchTag shouldBe testReference
      }
    }

    "CotaxSpecificData" - {

      val testCotaxSpecificData = CotaxSpecificData(testReference)

      "getReference should return the cotaxReference" in {
        testCotaxSpecificData.getReference shouldBe testReference
      }

      "getRawReference should return the cotaxReference with the last 7 characters dropped" in {
        testCotaxSpecificData.getRawReference shouldBe "someRe"
      }

      "searchTag should return the cotaxReference with the last 7 characters dropped" in {
        testCotaxSpecificData.searchTag shouldBe "someRe"
      }
    }

    "NtcSpecificData" - {

      val testNtcSpecificData = NtcSpecificData(testReference)

      "getReference should return the ntcReference" in {
        testNtcSpecificData.getReference shouldBe testReference
      }

      "getRawReference should return the ntcReference with the last 8 characters dropped" in {
        testNtcSpecificData.getRawReference shouldBe "someR"
      }

      "searchTag should return the ntcReference with the last 8 characters dropped" in {
        testNtcSpecificData.searchTag shouldBe "someR"
      }
    }

    "PayeSpecificData" - {

      val testPayeSpecificData = PayeSpecificData(testReference, BigDecimal(1), BigDecimal(1))

      "getReference should return the payeReference" in {
        testPayeSpecificData.getReference shouldBe testReference
      }

      "getRawReference should return the payeReference with the last four characters dropped" in {
        testPayeSpecificData.getRawReference shouldBe "someRefer"
      }

      "searchTag should return the payeReference with the last four characters dropped" in {
        testPayeSpecificData.searchTag shouldBe "someRefer"
      }
    }

    "NpsSpecificData" - {

      val testNpsSpecificData = NpsSpecificData(testReference, "1", "2", "3", BigDecimal(1))

      "getReference should return the npsReference" in {
        testNpsSpecificData.getReference shouldBe testReference
      }

      "getRawReference should return the npsReference with the last two characters dropped" in {
        testNpsSpecificData.getRawReference shouldBe "someReferen"
      }

      "searchTag should return the npsReference" in {
        testNpsSpecificData.searchTag shouldBe "someReference"
      }
    }

    "VatSpecificData" - {

      val testVatSpecificData = VatSpecificData(testReference, "someRemittanceType")

      "getReference should return the vatReference" in {
        testVatSpecificData.getReference shouldBe testReference
      }

      "getRawReference should return the vatReference with the last four characters dropped" in {
        testVatSpecificData.getRawReference shouldBe "someRefer"
      }

      "searchTag should return the vatReference" in {
        testVatSpecificData.searchTag shouldBe "someReference"
      }
    }

    "VatIossAndOssSpecificData" - {

      "when paymentType is Ioss" - {

        val testVatIossAndOssSpecificData = VatIossAndOssSpecificData(
          iossAndOssPaymentType = IossAndOssPaymentTypes.Ioss,
          vatIossRegistrationNumber = Some(IossRegistrationNumber("IM1234567891")),
          vatIossPeriod = Some(CalendarPeriod(1, 2027)),
          vatOssVrn = None,
          vatOssPeriod = None,
          vatIossOssPenaltyReference = None
        )

        "getReference" - {

          "should return the vatIossRegistrationNumber and vatIossPeriod formatted as IMNNNNNNNNNNMDDDD" in {
            testVatIossAndOssSpecificData.getReference shouldBe "IM1234567891M0127"
          }

          "when vatIossRegistrationNumber is none" in {
            val error: IllegalStateException = intercept[IllegalStateException](
              testVatIossAndOssSpecificData.copy(vatIossRegistrationNumber = None).getReference
            )
            error.getMessage shouldBe "vatIossRegistrationNumber and vatIossPeriod are required for IOSS payment type"
          }

          "when period is none" in {
            val error: IllegalStateException = intercept[IllegalStateException](
              testVatIossAndOssSpecificData.copy(vatIossPeriod = None).getReference
            )
            error.getMessage shouldBe "vatIossRegistrationNumber and vatIossPeriod are required for IOSS payment type"
          }
        }

        "getRawReference should return the vatIossRegistrationNumber and vatIossPeriod formatted as IMNNNNNNNNNNMDDDD" in {
          testVatIossAndOssSpecificData.getRawReference shouldBe "IM1234567891M0127"
        }

        "searchTag" - {

          "should return the vatIossRegistrationNumber" in {
            testVatIossAndOssSpecificData.searchTag shouldBe "IM1234567891"
          }

          "should throw an error when vatIossRegistrationNumber is none" in {
            val error: IllegalStateException = intercept[IllegalStateException](
              testVatIossAndOssSpecificData.copy(vatIossRegistrationNumber = None).searchTag
            )
            error.getMessage shouldBe "vatIossRegistrationNumber is required for IOSS payment type"
          }
        }
      }

      "when paymentType is Oss" - {

        val testVatIossAndOssSpecificData = VatIossAndOssSpecificData(
          iossAndOssPaymentType = IossAndOssPaymentTypes.Oss,
          vatIossRegistrationNumber = None,
          vatIossPeriod = None,
          vatOssVrn = Some(OssVrn("968501144")),
          vatOssPeriod = Some(CalendarQuarterlyPeriod(AprilToJune, 2026)),
          vatIossOssPenaltyReference = None
        )

        "getReference" - {

          "should return the vatOssVrn and vatOssPeriod formatted as NI + VRN + Q + (Quarter) + YY" in {
            testVatIossAndOssSpecificData.getReference shouldBe "NI968501144Q226"
          }

          "throw an error" - {

            "when vrn is none" in {
              val error: IllegalStateException = intercept[IllegalStateException](
                testVatIossAndOssSpecificData.copy(vatOssVrn = None).getReference
              )
              error.getMessage shouldBe "vatOssVrn and vatOssPeriod are required for OSS payment type"
            }

            "when period is none" in {
              val error: IllegalStateException = intercept[IllegalStateException](
                testVatIossAndOssSpecificData.copy(vatOssPeriod = None).getReference
              )
              error.getMessage shouldBe "vatOssVrn and vatOssPeriod are required for OSS payment type"
            }
          }
        }

        "getRawReference should return the vatOssVrn and vatOssPeriod formatted as NI + VRN + Q + (Quarter) + YY" in {
          testVatIossAndOssSpecificData.getRawReference shouldBe "NI968501144Q226"
        }

        "searchTag" - {
          "should return the vatOssVrn" in {
            testVatIossAndOssSpecificData.searchTag shouldBe "968501144"
          }
          "should throw an error when vatOssVrn is none" in {
            val error = intercept[IllegalStateException](testVatIossAndOssSpecificData.copy(vatOssVrn = None).searchTag)
            error.getMessage shouldBe "vatOssVrn is required for OSS payment type"
          }
        }
      }

      "when paymentType is ChargeReference" - {

        val testVatIossAndOssSpecificData = VatIossAndOssSpecificData(
          iossAndOssPaymentType = IossAndOssPaymentTypes.ChargeReference,
          vatIossRegistrationNumber = None,
          vatIossPeriod = None,
          vatOssVrn = None,
          vatOssPeriod = None,
          vatIossOssPenaltyReference = Some(IossOssChargeReference("XE123456789012"))
        )

        "getReference" - {

          "should return the vatIossOssPenaltyReference" in {
            testVatIossAndOssSpecificData.getReference shouldBe "XE123456789012"
          }

          "should throw an error when vatIossOssPenaltyReference is none" in {
            val error: IllegalStateException = intercept[IllegalStateException](
              VatIossAndOssSpecificData(
                iossAndOssPaymentType = IossAndOssPaymentTypes.ChargeReference,
                vatIossRegistrationNumber = None,
                vatIossPeriod = None,
                vatOssVrn = None,
                vatOssPeriod = None,
                vatIossOssPenaltyReference = None
              ).getReference
            )
            error.getMessage shouldBe "vatIossOssPenaltyReference is required for ChargeReference payment type"
          }
        }

        "getRawReference should return the vatIossOssPenaltyReference" in {
          testVatIossAndOssSpecificData.getRawReference shouldBe "XE123456789012"
        }

        "searchTag" - {

          "should return the vatIossOssPenaltyReference" in {
            testVatIossAndOssSpecificData.searchTag shouldBe "XE123456789012"
          }

          "should throw an error when vatIossOssPenaltyReference is none" in {
            val error = intercept[IllegalStateException](testVatIossAndOssSpecificData.copy(vatIossOssPenaltyReference = None).searchTag)
            error.getMessage shouldBe "vatIossOssPenaltyReference is required for ChargeReference payment type"
          }
        }
      }
    }

    "PptSpecificData" - {

      val testPptSpecificData = PptSpecificData(testReference)

      "getReference should return the pptReference" in {
        testPptSpecificData.getReference shouldBe testReference
      }

      "getRawReference should return the pptReference" in {
        testPptSpecificData.getRawReference shouldBe testReference
      }

      "searchTag should return the pptReference" in {
        testPptSpecificData.searchTag shouldBe testReference
      }
    }

    "Reads[PaymentSpecificData] should throw JsResultException('Could not read PaymentSpecificData') when unknown PaymentSpecificData" in {
      intercept[JsResultException] {
        JsString("invalid").as[PaymentSpecificData]
      }.getMessage.contains("Could not read PaymentSpecificData") shouldBe true
    }
  }
}
