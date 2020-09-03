package net.woggioni.hello.jpms

import net.woggioni.hello.jpms.a.A
import net.woggioni.hello.jpms.a.P2d

object Hello {

    @JvmStatic
    fun main(args: Array<String>) {
        val p1 = P2d(1, 2)
        val p2 = P2d(3, 4)
        println(A.distance(p1, p2))
    }

}