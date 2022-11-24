package utils

import kotlin.concurrent.timer

object TimerCallBack {
    val subscriptions = ArrayList<()->Unit>()

    fun bind(f: ()->Unit) {
        subscriptions.add(f)
    }

    fun start(){
        timer(initialDelay = 60000, period = 1000){
            subscriptions.forEach {
                it()
            }
        }
    }
}