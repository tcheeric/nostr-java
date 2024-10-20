package nostr.test.client;

/**
 * @author squirrel
 */
class ClientTest {
    /*

    private Client client;
    private Identity identity;

    public ClientTest() {
    }

    @BeforeEach
    public void init() {
        System.out.println("init");
        identity = Identity.create(PrivateKey.generateRandomPrivKey());

        var requestContext = new DefaultRequestContext();
        requestContext.setPrivateKey(identity.getPrivateKey().getRawData());
        requestContext.setRelays(Map.of("My local test relay", "ws://localhost:5555"));
        try {
            client = Client.getInstance().connect(requestContext);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    //@AfterEach
    public void dispose() {
        System.out.println("dispose");
        try {
            this.client.disconnect();
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
        this.client = null;
        this.identity = null;
    }

    @Test
    public void testSend() throws TimeoutException {
        System.out.println("testSend");
        var event = EntityFactory.Events.createTextNoteEvent(identity.getPublicKey());
        identity.sign(event);
        BaseMessage msg = new EventMessage(event);

        client.send(msg);

        assertTrue(true);
    }

    @Test
    public void disconnect() throws TimeoutException {
        System.out.println("disconnect");

        var relayCount = getRelayCount();
        Assertions.assertEquals(relayCount, client.getOpenConnectionsCount());
        client.disconnect();

        Assertions.assertEquals(0, client.getOpenConnectionsCount());
    }

    @Test
    public void testNip42() throws TimeoutException {
        System.out.println("testNip42");

        var rcpt = Identity.generateRandomIdentity().getPublicKey();
        var sender = identity.getPublicKey();
        var event = EntityFactory.Events.createDirectMessageEvent(sender, rcpt, "Hello, World!");
        identity.sign(event);
        BaseMessage msg = new EventMessage(event);
        client.send(msg);

        var filters = Filters.builder().kinds(new KindList(4)).authors(new PublicKeyList(sender)).build();
        msg = new ReqMessage("testNip42_" + sender.toString(), filters);
        client.send(msg);
    }

    private int getRelayCount() {
        Properties properties = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("relays.properties")) {
            if (is != null) {
                properties.load(is);
            } else {
                throw new IOException("Cannot find relays.properties on the classpath");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.size();
    }
    */
}
