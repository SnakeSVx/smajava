package be.svx.sma.core;

import java.util.List;

/**
 * Created by Stijn on 16/02/14.
 */
public interface Request extends Packet {

    boolean isInternal();

    boolean isBlocking();

    List<Class> responseClass();

}
