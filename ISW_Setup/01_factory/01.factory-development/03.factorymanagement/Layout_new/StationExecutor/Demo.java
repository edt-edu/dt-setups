package Layout_new.StationExecutor;

import Layout_new.IllegalActionException;
import Layout_new.InputOutputStation;
import Layout_new.Yoghurt;

import java.util.concurrent.TimeUnit;

public class Demo implements Runnable {

    FreezingExecutorLogic freeze;
    ToppingExecutorLogic tel;

    Integer count = 0;

    public Demo() {
        //TODO
        //Server der JSON von Yoghurt Orders empfängt
        //dann mapping auf nötige stationen
        //putInputObject von IOStation nutzen um vom TB hintransportiertes Objekt anzuzeigen
        //in Executor Logik aufgrund Attribute des InputObjekts ausführung anpassen
        InputOutputStation ioStation = new InputOutputStation(1);
        this.tel = new ToppingExecutorLogic(ioStation);
        InputOutputStation ioStation2 = new InputOutputStation(1);
        this.freeze = new FreezingExecutorLogic(ioStation2);
    }

    public static void main(String[] args) throws IllegalActionException {
        Demo d = new Demo();
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //Thread i1
        Thread i2 = new Thread(d.freeze);
        Thread i4 = new Thread(d.tel);
        Thread i = new Thread(d);
        //Thread refill = new Thread(d);
        i2.start();
        i4.start();
        i.start();
        //refill.start();
    }


    @Override
    public void run() {
        while(true){
            if (this.tel.getStepChainExecutorFinished() && this.tel.acceptsInput()){
                try {
                    this.tel.getIOStation().putInputObject(new Yoghurt());
                } catch (IllegalActionException e) {
                    throw new RuntimeException(e);
                }
            } else {
                if(count == 30000){
                    count = 0;
                    System.out.println("noRefill tel");
                } else {
                    count += 1;
                }
            }
            if (this.freeze.getStepChainExecutorFinished() && this.freeze.acceptsInput()){
                try {
                    this.freeze.getIOStation().putInputObject(new Yoghurt());
                } catch (IllegalActionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
