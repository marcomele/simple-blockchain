package me.blockchain.components;

import com.google.common.hash.Hashing;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import me.blockchain.model.Block;
import me.blockchain.model.Transaction;
import me.blockchain.util.JsonUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by marcomele on 5/10/20
 */

@Slf4j
@Component
public class Blockchain implements Iterable<Block> {

    private final Stack<Block> chain = new Stack<>();
    private final BlockingQueue<Transaction> currentTransactions = new LinkedBlockingDeque<>();
    private final AtomicLong blockIndexSequence;

    protected Blockchain() {
        final Block genesis = Block.genesis();
        blockIndexSequence = new AtomicLong(genesis.getIndex());
        chain.push(genesis);
    }

    @Synchronized
    public Block newBlock(final long proof, @Nullable final String previousHash) {
        final Block block = new Block();
        block.setIndex(blockIndexSequence.incrementAndGet());
        block.setTimestamp(DateTime.now(DateTimeZone.UTC).getMillis());
        final List<Transaction> transactions = new ArrayList<>();
        currentTransactions.drainTo(transactions);
        block.setTransactions(transactions);
        block.setProof(proof);
        block.setPreviousHash(null != previousHash ? previousHash : hash(chain.peek()));
        chain.push(block);
        return block;
    }

    public long newTransaction(final String sender, final String recipient, final double amount) {
        currentTransactions.add(new Transaction(sender, recipient, amount));
        return chain.lastElement().getIndex() + 1;
    }

    @Synchronized
    public void replaceBlockchain(@NotNull final List<Block> chain) {
        this.chain.clear();
        chain.forEach(this.chain::push);
    }

    public Block lastBlock() {
        return chain.peek();
    }

    public List<Block> getChain() {
        return Collections.unmodifiableList(chain);
    }

    public static String hash(final Block block) {
        final String serialized = JsonUtil.serialize(block);
        return Hashing.sha256().hashString(serialized, StandardCharsets.UTF_8).toString();
    }

    public static long proofOfWork(final long lastProof) {
        long proof;
        log.info("Starting mining new block");
        StopWatch watch = new StopWatch();
        watch.start();
        for (proof = 0; !validateProof(lastProof, proof); proof++)
            ;
        watch.stop();
        log.info("Mined next block in {}s", watch.getTotalTimeSeconds());
        return proof;
    }

    public static boolean validateProof(final long lastProof, final long proof) {
        final String guess = String.format("%d%d", lastProof, proof);
        final String hash = Hashing.sha256().hashString(guess, StandardCharsets.UTF_8).toString();
        return hash.indexOf("0000") == 0;
    }

    @Override
    public Iterator<Block> iterator() {
        return chain.iterator();
    }

}
