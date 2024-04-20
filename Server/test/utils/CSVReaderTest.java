package utils;

import IPSwitch.InterContinentalRouter;
import IPSwitch.Packet;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CSVReaderTest {
    Map<String, InterContinentalRouter> interRouters;

    // create sample
    Packet sample = new Packet(1, "abc", 1, 2, "sampleMsgId", 1, IPSwitch.MessageType.TEXT, "destNetIdFull");

    @Test
    void readIntercontinentalRoutersCSV() {
        try {
            interRouters = CSVReader.readIntercontinentalRoutersCSV();
            assertFalse(interRouters.isEmpty(), "The list of routers should not be empty");

            // Print all routers
            for (InterContinentalRouter router : interRouters.values()) {
                System.out.println(router.toString());
            }

        } catch (IOException e) {
            fail("An IOException was thrown: " + e.getMessage());
        }
        assertEquals(interRouters.size(), 4);
    }

    /**
     * Testing queue functionality and object retrieval
     */
    @Test
    void queueTest() {
        try {
            interRouters = CSVReader.readIntercontinentalRoutersCSV();
            assertFalse(interRouters.isEmpty(), "The list of routers should not be empty");

            for (InterContinentalRouter router : interRouters.values()) {
                System.out.println(router.toString());
            }

            InterContinentalRouter munichRouter = interRouters.get("munich");
            assertNotNull(munichRouter, "The router should not be null");

            // Enqueue a packet
            munichRouter.enqueue(sample);
            assertEquals(munichRouter.getQueue().size(), 1, "The queue should have one packet");

            // Dequeue a packet
            Packet dequeued = munichRouter.dequeue();
            assertEquals(dequeued, sample, "The dequeued packet should be the same as the sample packet");

            // Enqueue multiple packets
            List<Packet> packets = Arrays.asList(
                    new Packet(2, "def", 1, 2, "sampleMsgId", 1, IPSwitch.MessageType.TEXT, "destNetIdFull"),
                    new Packet(3, "ghi", 1, 2, "sampleMsgId", 1, IPSwitch.MessageType.TEXT, "destNetIdFull")
            );
            munichRouter.enqueue(packets.get(0));
            munichRouter.enqueue(packets.get(1));
            assertEquals(munichRouter.getQueue().size(), 2, "The queue should have two packets");

            // Dequeue multiple packets
            Packet dequeued1 = munichRouter.dequeue();
            assertEquals(dequeued1, packets.get(0), "The dequeued packet should be the same as the first packet");
            Packet dequeued2 = munichRouter.dequeue();
            assertEquals(dequeued2, packets.get(1), "The dequeued packet should be the same as the second packet");

        } catch (IOException e) {
            fail("An IOException was thrown: " + e.getMessage());
        }
    }
}