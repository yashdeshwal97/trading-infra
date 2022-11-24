package services

import mu.KLogging

class RateLimiter {

    companion object: KLogging()

    var callManager: SumTimed
    var callLimit: Int
    var callDuration: Double

    constructor(callLimit: Int, callDuration: Double){
        logger.info{"Initializing CallManager"}
        this.callDuration = callDuration
        this.callManager = SumTimed(callDuration)
        this.callLimit = callLimit
    }

    fun addCall(count: Int){
        callManager.add(count)
    }

    fun callAllowed(count: Int) : Boolean {
        return (callManager.value.toInt() + count <= callLimit)
    }

    override fun toString(): String {
        return "CallManager(callManager=$callManager, callLimit=$callLimit, callDuration=$callDuration)"
    }
}