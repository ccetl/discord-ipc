package ccetl.discordipc;

import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.newsclub.net.unix.AFUNIXSocketChannel;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.util.function.Consumer;

public class UnixConnection extends Connection {
    private final Selector s;
    private final AFUNIXSocketChannel sc;
    private final Consumer<Packet> callback;

    public UnixConnection(File file, Consumer<Packet> callback) throws IOException {
        this.s = Selector.open();
        this.sc = AFUNIXSocketChannel.open(AFUNIXSocketAddress.of(file));
        this.callback = callback;

        sc.configureBlocking(false);
        sc.register(s, SelectionKey.OP_READ);

        Thread thread = new Thread(this::run);
        thread.setName("Discord IPC - Read thread");
        thread.start();
    }

    private void run() {
        State state = State.Opcode;

        ByteBuffer intB = ByteBuffer.allocate(4);
        ByteBuffer dataB = null;

        Opcode opcode = null;

        try {
            while (true) {
                s.select();

                switch (state) {
                    case Opcode:
                        sc.read(intB);
                        if (intB.hasRemaining()) {
                            break;
                        }

                        opcode = Opcode.valueOf(Integer.reverseBytes(intB.getInt(0)));
                        state = State.Length;

                        intB.rewind();
                        break;
                    case Length:
                        sc.read(intB);
                        if (intB.hasRemaining()) {
                            break;
                        }

                        dataB = ByteBuffer.allocate(Integer.reverseBytes(intB.getInt(0)));
                        state = State.Data;

                        intB.rewind();
                        break;
                    case Data:
                        sc.read(dataB);
                        if (dataB.hasRemaining()) {
                            break;
                        }

                        String data = Charset.defaultCharset().decode((ByteBuffer) dataB.rewind()).toString();
                        callback.accept(new Packet(opcode, DiscordIPC.jsonParser.parse(data).getAsJsonObject()));

                        dataB = null;
                        state = State.Opcode;
                        break;
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void write(ByteBuffer buffer) {
        try {
            sc.write(buffer);
        } catch (IOException e) {
            DiscordIPC.getErrorCallback().error(e);
        }
    }

    @Override
    public void close() {
        try {
            s.close();
            sc.close();
        } catch (IOException e) {
            DiscordIPC.getErrorCallback().error(e);
        }
    }

    private enum State {
        Opcode,
        Length,
        Data
    }
}
