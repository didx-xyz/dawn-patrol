package xyz.didx.messages

case class Member(name: String, number: String)

case class SignalGroupInfo(
  groupId: String,
  `type`: String // DELIVER, UPDATE, QUIT, REQUEST_INFO, INFO
):
  override def toString(): String =
    s"""SignalGroupInfo(
      groupId: $groupId,
      type: ${`type`}
    )"""
