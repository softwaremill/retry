package retry

class PolicySpecJVM extends PolicySpec {
  implicit val timer = odelay.Timer.default
}
