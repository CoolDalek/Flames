package flames.concurrent.execution.collection

import org.scalatest.*
import org.scalatest.matchers.*
import org.scalatest.prop.*
import org.scalatest.propspec.*
import org.scalatestplus.scalacheck.*
import org.scalacheck.*
import flames.concurrent.execution.collection.ShrinkableMap

class ShrinkableMapTest extends AnyPropSpec, should.Matchers, ScalaCheckPropertyChecks:

  property("ShrinkableMap should insert values correctly") {
    forAll { (values: Map[String, String]) =>
      val shrinkableMap = ShrinkableMap.make[String, String]()
      values.foreach { (key, value) =>
        shrinkableMap.update(key, value)
      }
      shrinkableMap.size shouldEqual values.size
      shrinkableMap.foreach { (key, value) =>
        values.contains(key) shouldBe true
        values(key) shouldBe value
      }
    }
  }

end ShrinkableMapTest
