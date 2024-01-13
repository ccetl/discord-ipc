package ccetl.discordipc;

import com.google.gson.JsonObject;

@SuppressWarnings("unused")
public class Packet {
    private Opcode opcode;
    private JsonObject data;

    public Packet(Opcode opcode, JsonObject data) {
        this.opcode = opcode;
        this.data = data;
    }

    public Opcode getOpcode() {
        return opcode;
    }

    public void setOpcode(Opcode opcode) {
        this.opcode = opcode;
    }

    public JsonObject getData() {
        return data;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }
}
