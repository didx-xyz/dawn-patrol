package xyz.didx.registry

import cats.*
import cats.data.OptionT
import cats.syntax.all.*
import io.circe.Decoder.Result
import io.circe.*
import io.circe.syntax.*
import xyz.didx.didcomm.DIDDoc

/*
{
  "jobId": null,
  "didState": {
    "state": "finished",
    "secret": {
    },
    "did": "did:sov:danube:6m3NsHrDzi9F4ZpwQTGUHm"
  },
  "didRegistrationMetadata": {
    "duration": 3610,
    "method": "sov"
  },
  "didDocumentMetadata": {
    "network": "danube",
    "poolVersion": 2,
    "submitterDid": "V4SGRU86Z58d6TV7PBUe6f",
    "ledgerResult": {
      "result": {
        "reqSignature": {
          "type": "ED25519",
          "values": [
            {
              "value": "3XbBedfHfjBfMCYG2VsjnnFwTUugB4GmjyFK5hMcteGNAHUxuxecageKboMMTxPoZP6kHQ7AngUzteDwXcKgC7Xu",
              "from": "6m3NsHrDzi9F4ZpwQTGUHm"
            }
          ]
        },
        "txn": {
          "data": {
            "raw": "{\"endpoint\":{\"DecentralizedWebNode\":{\"nodes\":[\"https://dwn.example.com\",\"https://example.org/dwn\"]}}}",
            "dest": "6m3NsHrDzi9F4ZpwQTGUHm"
          },
          "type": "100",
          "protocolVersion": 2,
          "metadata": {
            "from": "6m3NsHrDzi9F4ZpwQTGUHm",
            "digest": "3d41637c745a3beb17eda0d391e42955692640b73cc577bba1d4ccd81dcc74fb",
            "reqId": 1679558958687242000,
            "payloadDigest": "02931c07d863ee3469c5954496d1c500aa0754dce1274f1bec8968f35ec31c98"
          }
        },
        "rootHash": "DvKtoYnSNRvzdMQdiDYvw9x4BgM3N6UVaEJeSqBZsKjz",
        "ver": "1",
        "txnMetadata": {
          "seqNo": 677,
          "txnId": "6m3NsHrDzi9F4ZpwQTGUHm:1:b6bf7bc8d96f3ea9d132c83b3da8e7760e420138485657372db4d6a981d3fd9e",
          "txnTime": 1679558961
        },
        "auditPath": [
          "6bLij8eH24Ye1XEg39uM4kFn5yTYT3HjwSLP9n32qcJa",
          "Gpj9pn2YQ95mgvtxWgoLRhif3B3B6MPeCQkeZRshFn5z",
          "8akwaCzKH18cg8frKmrHrNvx9ch2hWwzu4ZRQm1yYLc8",
          "5C8DxqxwGeSRM8JkAqMizQW931R9hknp5etaZceneqGo"
        ]
      },
      "op": "REPLY"
    }
  }
}
 */
case class RegistryResponse(
  jobId: Option[String],
  didState: DIDState,
  didRegistrationMetadata: DIDRegistrationMetadata,
  didDocumentMetadata: DIDDocumentMetadata
)

case class DIDState(
  state: String,
  secret: Option[Json],
  did: String
)

case class DIDRegistrationMetadata(
  duration: Int,
  method: String
)

case class DIDDocumentMetadata(
  network: String,
  poolVersion: Int,
  submitterDid: String,
  ledgerResult: LedgerResult
)

case class LedgerResult(
  result: Result
)

case class Result(
  reqSignature: ReqSignature,
  txn: Txn,
  rootHash: String,
  ver: String
)

case class ReqSignature(
  `type`: String,
  values: List[Values]
)

case class Values(
  value: String,
  from: String
)

case class Txn(
  data: Data,
  `type`: String,
  protocolVersion: Int,
  metadata: Metadata
)

case class Data(
  raw: String,
  dest: String
)

case class Metadata(
  from: String,
  digest: String
)

case class RegistryRequest(
  didDocument: DIDDoc,
  options: Map[String, String] = Map("network" -> "danube"),
  secret: Map[String, String] = Map.empty
)
