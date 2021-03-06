package com.ender.games.epoch.util

import com.ender.games.epoch.E_BRANCHES
import kotlin.math.absoluteValue
import kotlin.math.round
import kotlin.math.sqrt
import kotlin.random.Random

data class Room(val coord: HexCoord, val parent: Room?, val type: Int = Random(System.nanoTime()).nextInt(0, 6)) {
    val children: MutableList<Room> = mutableListOf()

    fun parentAt(dir: Int) = parent?.coord == coord + dirs[dir]!!
    fun hasChild(dir: Int) = children.map { it.coord }.contains(coord + dirs[dir]!!)

    // Testing only
    fun dups(): List<HexCoord> {
        return children.map { it.dups() }.flatten() + coord
    }

    override fun toString(): String {
        return "$coord $children"
    }
}

fun contains(head: Room, hCoord: HexCoord): Boolean {
    val frontier = mutableListOf(head)
    while(frontier.isNotEmpty()) {
        if(frontier[0].coord == hCoord) return true
        frontier.addAll(frontier.removeAt(0).children)
    }
    return false
}

fun count(head: Room): Int {
    val frontier = mutableListOf(head)
    var count = 0
    while(frontier.isNotEmpty()) {
        count++
        frontier.addAll(frontier.removeAt(0).children)
    }
    return count
}

data class Cube(val x: Float, val y: Float, val z: Float) {
    fun toHex() = cubeRound(this)
}

data class HexCoord(val x: Int, val y: Int, val z: Int) {
    fun toQR() = Pair(x, z)
}

fun fromQR(qr: Pair<Int, Int>) = HexCoord(qr.first, -qr.first - qr.second, qr.second)

operator fun HexCoord.plus(other: HexCoord) = HexCoord(this.x + other.x, this.y + other.y, this.z + other.z)
val HEX_ZERO = HexCoord(0, 0, 0)
val dirs = mapOf(
        0 to HexCoord(1, -1, 0),
        5 to HexCoord(1, 0, -1),
        4 to HexCoord(0, 1, -1),
        3 to HexCoord(-1, 1, 0),
        2 to HexCoord(-1, 0, 1),
        1 to HexCoord(0, -1, 1),
)

fun Pair<Int, Int>.toPoint(): Pair<Float, Float> {
    val x =  (sqrt(3.0) * this.first + sqrt(3.0) / 2f * this.second).toFloat()
    val y =  (                                    3.0  / 2f * this.second).toFloat()
    return Pair(x, y)
}

fun cubeRound(cube: Cube): HexCoord  {
    var rx = round(cube.x)
    var ry = round(cube.y)
    var rz = round(cube.z)

    val xDiff = (rx - cube.x).absoluteValue
    val yDiff = (ry - cube.y).absoluteValue
    val zDiff = (rz - cube.z).absoluteValue

    if(xDiff > yDiff && xDiff > zDiff) {
        rx = -ry - rz
    } else if (yDiff > zDiff) {
        ry = -rx - rz
    } else {
        rz = -rx - ry
    }

    return HexCoord(rx.toInt(), ry.toInt(), rz.toInt())
}

object HexMap {
    var seed = System.currentTimeMillis()
    private val rnd = Random(seed)
    val head = Room(HEX_ZERO, null, 0)

    fun gen(numRooms: Int) {
        head.children.clear()
        gen(numRooms, head)
    }

    private fun gen(numRooms: Int, prev: Room) {
        var nr = numRooms
        for(i in 0..5) {
            if(nr <= 0) break
            if(rnd.nextFloat() <= E_BRANCHES / 6.0) {
                if(!contains(head, prev.coord + dirs[i]!!)) {
                    prev.children.add(Room(prev.coord + dirs[i]!!, prev))
                    nr--
                }
            }
        }

        if(nr > 0) {
            if(prev.children.isEmpty()) {
                // If the room has an allocated number of rooms to spawn and hasn't spawned any, force one if possible
                for(i in 0..5) {
                    if(!contains(head, prev.coord + dirs[i]!!)) {
                        prev.children.add(Room(prev.coord + dirs[i]!!, prev))
                        nr--
                    }
                }
                // If the room cannot allocate any, propagate up. If the room in question is the head, give up
                // if(!p) if(prev.parent != head) prev.parent?.let { gen(nr, it) } else return
            } else {
                val dChildRs =
                        MutableList(prev.children.size) { rnd.nextDouble(0.0, 1.0) }
                val pSum = dChildRs.sum()
                val iChildRs = dChildRs.map { it / pSum * nr }.map { it.toInt() } as MutableList<Int>
                iChildRs[iChildRs.lastIndex] = nr - iChildRs.dropLast(1).sum()
                prev.children.forEachIndexed { idx, child ->
                    gen(iChildRs[idx], child)
                }
            }
        }

    }
}

fun main() {
    for(i in 0..100) {
        HexMap.gen(100)
        val x = HexMap.head.dups()
        if(x.distinct() != x) {
            println(HexMap.head)
        }
    }
}