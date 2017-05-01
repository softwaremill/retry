package retry

class PolicySpecJS extends PolicySpec {
  implicit val timer = new odelay.js.JsTimer()
}

