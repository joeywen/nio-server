package com.joey.worker;

import com.joey.common.Pair;
import com.joey.common.ProtoHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.concurrent.Callable;

/**
 * A single thread process every impala query and write back the query result
 *
 * Created by joey on 14-7-2.
 */
public class QueryWorker extends AbstractWorker implements Callable<Pair> {
    private final Logger logger = LoggerFactory.getLogger(QueryWorker.class);

    public QueryWorker(Pair pair) {
        this.pair = pair;
    }

    @Override
    /**
     * main method to execute the query command, pack the result
     * and write it back to client
     */
    public Pair call() throws Exception {
        SelectionKey key = (SelectionKey) this.pair.getKey();
        Pair<ProtoHeader, ByteBuffer> message = (Pair<ProtoHeader, ByteBuffer>) this.pair.getValue();
        // TODO invoke impala query method, process message
        // message = invoke_impala_method(cmd)
        write(key, message);
        return null;
    }
}
