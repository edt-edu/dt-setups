package dts.modelmanager.implementations.postgres

import java.util.concurrent.TimeUnit

class ThreadTesting(val name11:String):Thread() {
    var isrunning = false
    override fun run() {
        isrunning = true
        println("Thread $name11 started")
        while (true) {
            if (interrupted()) {
                break;
            }
            //println("running $name11 \n")
            //TimeUnit.SECONDS.sleep(1);
        }
        isrunning = false
        println("Thread $name11 stopped")
    }
}

fun main() {
    var stop = false
    var ths = mutableMapOf<String, Thread>()

    while (!stop){
        var input = readLine()
        if(input.equals("stop")) {
            stop = true

            for (key in ths.keys){
                ths.get(key)!!.interrupt()
            }
            continue
        }

        var cmd = input!!.split(" ")
        if (cmd.size != 2) {
            println("input error: Exaclty 2 Arguments. \n Commands: create or destroy + Threadname")
            continue
        }
        else if (cmd[0] == "create") {
            var t = ThreadTesting(cmd[1])
            ths.put(cmd[1], t)
            ths.get(cmd[1])!!.start()
        }
        else if (cmd[0] == "destroy") {
            ths.get(cmd[1])!!.interrupt()
        }

        TimeUnit.SECONDS.sleep(1);
    }
}