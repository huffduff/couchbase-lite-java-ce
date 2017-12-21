package com.couchbase.lite;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ReplicatorTest extends BaseReplicatorTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testBadURL() throws InterruptedException {
        ReplicatorConfiguration config = makeConfig(false, true, false, "blxp://localhost/db");
        run(config, 15, "LiteCore");
    }

    @Test
    public void testEmptyPush() throws InterruptedException {
        ReplicatorConfiguration config = makeConfig(true, false, false);
        run(config, 0, null);
    }

    @Test
    public void testPushDoc() throws Exception {
        MutableDocument doc1 = new MutableDocument("doc1");
        doc1.setValue("name", "Tiger");
        save(doc1);
        assertEquals(1, db.getCount());

        MutableDocument doc2 = new MutableDocument("doc2");
        doc2.setValue("name", "Cat");
        otherDB.save(doc2);
        assertEquals(1, otherDB.getCount());

        ReplicatorConfiguration config = makeConfig(true, false, false);
        run(config, 0, null);

        assertEquals(2, otherDB.getCount());
        Document doc2a = otherDB.getDocument("doc2");
        assertEquals("Cat", doc2a.getString("name"));
    }

    @Test
    public void testPushDocContinuous() throws Exception {
        MutableDocument doc1 = new MutableDocument("doc1");
        doc1.setValue("name", "Tiger");
        save(doc1);
        assertEquals(1, db.getCount());

        MutableDocument doc2 = new MutableDocument("doc2");
        doc2.setValue("name", "Cat");
        otherDB.save(doc2);
        assertEquals(1, otherDB.getCount());

        ReplicatorConfiguration config = makeConfig(true, false, true);
        run(config, 0, null);

        assertEquals(2, otherDB.getCount());
        Document doc2a = otherDB.getDocument("doc2");
        assertEquals("Cat", doc2a.getString("name"));
    }

    @Test
    public void testPullDoc() throws Exception {
        // For https://github.com/couchbase/couchbase-lite-core/issues/156
        MutableDocument doc1 = new MutableDocument("doc1");
        doc1.setValue("name", "Tiger");
        save(doc1);
        assertEquals(1, db.getCount());

        MutableDocument doc2 = new MutableDocument("doc2");
        doc2.setValue("name", "Cat");
        otherDB.save(doc2);
        assertEquals(1, otherDB.getCount());

        ReplicatorConfiguration config = makeConfig(false, true, false);
        run(config, 0, null);

        assertEquals(2, db.getCount());
        Document doc2a = db.getDocument("doc2");
        assertEquals("Cat", doc2a.getString("name"));
    }

    // https://github.com/couchbase/couchbase-lite-core/issues/156
    @Test
    public void testPullDocContinuous() throws Exception {
        MutableDocument doc1 = new MutableDocument("doc1");
        doc1.setValue("name", "Tiger");
        save(doc1);
        assertEquals(1, db.getCount());

        MutableDocument doc2 = new MutableDocument("doc2");
        doc2.setValue("name", "Cat");
        otherDB.save(doc2);
        assertEquals(1, otherDB.getCount());

        ReplicatorConfiguration config = makeConfig(false, true, true);
        run(config, 0, null);

        assertEquals(2, db.getCount());
        Document doc2a = db.getDocument("doc2");
        assertEquals("Cat", doc2a.getString("name"));
    }

    @Test
    public void testPullConflict() throws Exception {
        MutableDocument doc1 = new MutableDocument("doc");
        doc1.setValue("species", "Tiger");
        save(doc1);
        assertEquals(1, db.getCount());
        doc1.setValue("name", "Hobbes");
        save(doc1);
        assertEquals(1, db.getCount());

        MutableDocument doc2 = new MutableDocument("doc");
        doc2.setValue("species", "Tiger");
        otherDB.save(doc2);
        assertEquals(1, otherDB.getCount());
        doc2.setValue("pattern", "striped");
        otherDB.save(doc2);
        assertEquals(1, otherDB.getCount());

        ReplicatorConfiguration config = makeConfig(false, true, false);
        run(config, 0, null);
        assertEquals(1, db.getCount());

        Document doc1a = db.getDocument("doc");
        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("species", "Tiger");
        expectedMap.put("name", "Hobbes");
        expectedMap.put("pattern", "striped");
        assertEquals(expectedMap, doc1a.toMap());
    }

    //TODO - port from ios
    @Test
    public void testPullConflictNoBaseRevision() {
    }

    //TODO - port from ios
    @Test
    public void testStopContinuousReplicator() {
    }

    @Test
    public void testDocIDFilter() throws CouchbaseLiteException, InterruptedException {
        MutableDocument doc1 = new MutableDocument("doc1");
        doc1.setString("species", "Tiger");
        db.save(doc1);
        doc1.setString("name", "Hobbes");
        db.save(doc1);

        MutableDocument doc2 = new MutableDocument("doc2");
        doc2.setString("species", "Tiger");
        db.save(doc2);
        doc2.setString("pattern", "striped");
        db.save(doc2);

        MutableDocument doc3 = new MutableDocument("doc3");
        doc3.setString("species", "Tiger");
        otherDB.save(doc3);
        doc3.setString("name", "Hobbes");
        otherDB.save(doc3);

        MutableDocument doc4 = new MutableDocument("doc4");
        doc4.setString("species", "Tiger");
        otherDB.save(doc4);
        doc4.setString("pattern", "striped");
        otherDB.save(doc4);

        ReplicatorConfiguration config = makeConfig(true, true, false);
        config.setDocumentIDs(Arrays.asList("doc1", "doc3"));
        run(config, 0, null);
        assertEquals(3, db.getCount());
        assertNotNull(db.getDocument("doc3"));
        assertEquals(3, otherDB.getCount());
        assertNotNull(otherDB.getDocument("doc1"));
    }

    @Test
    public void testReplicatorStopWhenClosed() throws CouchbaseLiteException {
        ReplicatorConfiguration config = makeConfig(true, true, true);
        Replicator repl = new Replicator(config);
        repl.start();
        while (repl.getStatus().getActivityLevel() != Replicator.ActivityLevel.IDLE) {
            Log.w(TAG, String.format(Locale.ENGLISH,
                    "Replicator status is still %s, waiting for idle...",
                    repl.getStatus().getActivityLevel()));
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
        reopenDB();
        int attemptCount = 0;
        while (attemptCount++ < 10 && repl.getStatus().getActivityLevel() != Replicator.ActivityLevel.STOPPED) {
            Log.w(TAG, String.format(Locale.ENGLISH,
                    "Replicator status is still %s, waiting for stopped (remaining attempts %d)...",
                    repl.getStatus().getActivityLevel(), 10 - attemptCount));
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
        assertTrue(attemptCount < 10);
    }
}
