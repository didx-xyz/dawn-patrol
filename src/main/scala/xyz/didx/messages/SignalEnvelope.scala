package xyz.didx.messages

case class SignalEnvelope(
  source: String,
  sourceNumber: String,
  sourceUuid: String,
  sourceName: String,
  sourceDevice: Int,
  timestamp: Long,
  dataMessage: Option[SignalDataMessage],
  receiptMessage: Option[SignalReceiptMessage],
  syncMessage: Option[SignalSyncMessage],
  sentMessage: Option[SignalSentMessage]
):
  override def toString(): String =
    s"""SignalEnvelope(source: $source,
      sourceNumber: $sourceNumber,
      sourceUuid: $sourceUuid,
      sourceName: $sourceName,
      sourceDevice: $sourceDevice,
      timestamp: $timestamp,
      dataMessage: $dataMessage,
      receiptMessage: $receiptMessage,
      syncMessage: $syncMessage,
      sentMessage: $sentMessage
    )"""

sealed trait Message

case class SignalDataMessage(
  timestamp: Long,
  message: String,
  expiresInSeconds: Int,
  viewOnce: Boolean,
  groupInfo: Option[SignalGroupInfo]
) extends Message:
  override def toString(): String =
    s"""SignalDataMessage(
      timestamp: $timestamp,
      message: $message,
      expiresInSeconds: $expiresInSeconds,
      viewOnce: $viewOnce,
      groupInfo: $groupInfo
    )"""

case class SignalReceiptMessage(
  when: Long,
  isDelivery: Boolean,
  isRead: Boolean,
  isViewed: Boolean,
  timestamps: List[Long]
) extends Message:
  override def toString(): String =
    s"""SignalReceiptMessage(
      when: $when,
      isDelivery: $isDelivery,
      isRead: $isRead,
      isViewed: $isViewed,
      timestamps: $timestamps
    )"""

case class SignalSyncMessage(
  sentMessage: Option[SignalSentMessage],
  readMessages: Option[List[SignalReadMessage]]
) extends Message:
  override def toString(): String =
    s"""SignalSyncMessage(
      sentMessage: $sentMessage,
      readMessages: ${readMessages.mkString(",")}
    )"""

case class SignalSentMessage(
  destination: String,
  destinationNumber: String,
  destinationUuid: String,
  timestamp: Long,
  message: String,
  expiresInSeconds: Int,
  viewOnce: Boolean,
  groupInfo: Option[SignalGroupInfo]
) extends Message:
  override def toString(): String =
    s"""SignalSentMessage(
      destination: $destination,
      destinationNumber: $destinationNumber,
      destinationUuid: $destinationUuid,
      timestamp: $timestamp,
      message: $message,
      expiresInSeconds: $expiresInSeconds,
      viewOnce: $viewOnce,
      groupInfo: $groupInfo
    )"""

case class SignalReadMessage(
  sender: String,
  senderNumber: String,
  senderUuid: String,
  timestamp: Long
) extends Message:
  override def toString(): String =
    s"""SignalReadMessage(
      sender: $sender,
      senderNumber: $senderNumber,
      senderUuid: $senderUuid,
      timestamp: $timestamp
    )"""
