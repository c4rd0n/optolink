package de.myandres.optolink.viessmann;

import java.util.concurrent.Semaphore;

public class ViessmannSemaphore extends Semaphore {
    private static ViessmannSemaphore instance= new ViessmannSemaphore(1);

    private ViessmannSemaphore(int permits){
        super(permits);
    }

    public static ViessmannSemaphore getInstance(){
            return instance;
    }
}
