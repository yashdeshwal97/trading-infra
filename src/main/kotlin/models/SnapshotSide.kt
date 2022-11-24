package models

import models.Snaplevel
import constants.enums.Side

class SnapshotSide (val side: Side){
    var snap: MutableList<Snaplevel> = mutableListOf()

    fun addLevel(price: Double, amount: Double) {
        val level = Snaplevel(price, amount)
        snap.add(level)
    }
}