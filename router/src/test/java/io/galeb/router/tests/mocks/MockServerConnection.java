package io.galeb.router.tests.mocks;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import org.xnio.ChannelListener;
import org.xnio.Option;
import org.xnio.OptionMap;
import org.xnio.Pool;
import org.xnio.StreamConnection;
import org.xnio.XnioIoThread;
import org.xnio.XnioWorker;
import org.xnio.channels.ConnectedChannel;
import org.xnio.conduits.ConduitStreamSinkChannel;
import org.xnio.conduits.ConduitStreamSourceChannel;
import org.xnio.conduits.StreamSinkConduit;

import io.undertow.UndertowMessages;
import io.undertow.connector.ByteBufferPool;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.HttpUpgradeListener;
import io.undertow.server.SSLSessionInfo;
import io.undertow.server.ServerConnection;
import io.undertow.server.XnioBufferPoolAdaptor;

public class MockServerConnection extends ServerConnection {
    private final ByteBufferPool bufferPool;
    private SSLSessionInfo sslSessionInfo;
    private XnioBufferPoolAdaptor poolAdaptor;

    public MockServerConnection(ByteBufferPool bufferPool) {
        this.bufferPool = bufferPool;
    }

    @Override
    public Pool<ByteBuffer> getBufferPool() {
        if(poolAdaptor == null) {
            poolAdaptor = new XnioBufferPoolAdaptor(getByteBufferPool());
        }
        return poolAdaptor;
    }


    @Override
    public ByteBufferPool getByteBufferPool() {
        return bufferPool;
    }

    @Override
    public XnioWorker getWorker() {
        return null;
    }

    @Override
    public XnioIoThread getIoThread() {
        return null;
    }

    @Override
    public HttpServerExchange sendOutOfBandResponse(HttpServerExchange exchange) {
        throw UndertowMessages.MESSAGES.outOfBandResponseNotSupported();
    }

    @Override
    public boolean isContinueResponseSupported() {
        return false;
    }

    @Override
    public void terminateRequestChannel(HttpServerExchange exchange) {

    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean supportsOption(Option<?> option) {
        return false;
    }

    @Override
    public <T> T getOption(Option<T> option) throws IOException {
        return null;
    }

    @Override
    public <T> T setOption(Option<T> option, T value) throws IllegalArgumentException, IOException {
        return null;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public SocketAddress getPeerAddress() {
        return null;
    }

    @Override
    public <A extends SocketAddress> A getPeerAddress(Class<A> type) {
        return null;
    }

    @Override
    public ChannelListener.Setter<? extends ConnectedChannel> getCloseSetter() {
        return null;
    }

    @Override
    public SocketAddress getLocalAddress() {
        return null;
    }

    @Override
    public <A extends SocketAddress> A getLocalAddress(Class<A> type) {
        return null;
    }

    @Override
    public OptionMap getUndertowOptions() {
        return OptionMap.EMPTY;
    }

    @Override
    public int getBufferSize() {
        return 1024;
    }

    @Override
    public SSLSessionInfo getSslSessionInfo() {
        return sslSessionInfo;
    }

    @Override
    public void setSslSessionInfo(SSLSessionInfo sessionInfo) {
        sslSessionInfo = sessionInfo;
    }

    @Override
    public void addCloseListener(CloseListener listener) {
    }

    @Override
    public StreamConnection upgradeChannel() {
        return null;
    }

    @Override
    public ConduitStreamSinkChannel getSinkChannel() {
        return null;
    }

    @Override
    public ConduitStreamSourceChannel getSourceChannel() {
        return new ConduitStreamSourceChannel(null, null);
    }

    @Override
    protected StreamSinkConduit getSinkConduit(HttpServerExchange exchange, StreamSinkConduit conduit) {
        return conduit;
    }

    @Override
    protected boolean isUpgradeSupported() {
        return false;
    }

    @Override
    protected boolean isConnectSupported() {
        return false;
    }

    @Override
    protected void exchangeComplete(HttpServerExchange exchange) {
    }

    @Override
    protected void setUpgradeListener(HttpUpgradeListener upgradeListener) {
        //ignore
    }

    @Override
    protected void setConnectListener(HttpUpgradeListener connectListener) {
        //ignore
    }

    @Override
    protected void maxEntitySizeUpdated(HttpServerExchange exchange) {
    }

    @Override
    public String getTransportProtocol() {
        return "mock";
    }

    @Override
    public boolean isRequestTrailerFieldsSupported() {
        return false;
    }
}