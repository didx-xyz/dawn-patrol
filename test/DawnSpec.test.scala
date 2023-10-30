package xyz.didx
import munit.CatsEffectSuite


class DawnSpec extends CatsEffectSuite {
  test("hello world") {
    val x = 2
    println(x)
    assertEquals(1, 1)
  } 

}
