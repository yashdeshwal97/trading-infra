package services

import java.util.*

class SumTimed {

    private var durationMillis: Double
    private var sum: Double = 0.0
    private val queue = LinkedList<Pair<Long, Number>>()

    constructor(durationSeconds: Double){
        this.durationMillis = durationSeconds * 1000.0
    }

    fun add(value: Number) {
        val now = System.currentTimeMillis()

        while(queue.isNotEmpty()) {
            val first = queue.first
            if(now - first.first > durationMillis) {
                sum -= first.second.toDouble()
                queue.removeFirst()
            } else {
                break
            }
        }

        queue.addLast(Pair(now, value))
        sum += value.toDouble()
    }

    val count: Int
        get() {
            return queue.count()
        }

    val value : Number
        get() {

            val now = System.currentTimeMillis()

            while(queue.isNotEmpty()) {
                val first = queue.first
                if(now - first.first > durationMillis) {
                    sum -= first.second.toDouble()
                    queue.removeFirst()
                } else {
                    break
                }
            }

            return sum
        }

    fun changeDuration(durationSeconds: Double) {
        this.durationMillis = durationSeconds * 1000.0
    }

    override fun toString(): String {
        return "SumTimed(durationMillis=$durationMillis, sum=$sum, queue=$queue)"
    }

}