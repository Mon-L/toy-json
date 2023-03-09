package cn.zcn.json.stream;

import java.io.IOException;
import java.io.Reader;

import static cn.zcn.json.ast.JsonCharacters.*;
import static cn.zcn.json.ast.JsonCharacters.TAB;

/**
 * Abstract reader
 *
 * @author zicung
 */
public class AbstractReader {

    /**
     * Json content stream
     */
    private final Reader reader;

    /**
     * 字符缓存，用于缓存从 {@code Reader} 读取的 chars
     */
    protected final char[] readBuffer = new char[512];

    /**
     * 下一个将要读取的 char 的索引位置
     */
    protected int nextPos = 0;

    /**
     * {@code readBuffer} 缓存的字符数量
     */
    protected int fill = 0;

    /**
     * Json Value buffer。当在读取 Json Value 时读取到了 {@code readBuffer} 的尾部时，当前 Json Value 尚未读取完成时，将内容缓存到 {@code valueBuffer}
     */
    protected StringBuilder valueBuffer;

    /**
     * 标识当前读取的 Json Value 的开始索引
     */
    protected int valueStartPos = -1;

    /**
     * 当前读取的 char
     */
    protected int current = 0;

    protected int line = 1;

    protected int column = 0;

    public AbstractReader(Reader reader) {
        this.reader = reader;
    }

    protected void readNext() throws IOException {
        if (nextPos == fill) {
            if (valueStartPos != -1) {
                valueBuffer.append(readBuffer, valueStartPos, fill - valueStartPos);
                valueStartPos = 0;
            }

            //读取字符到缓存中
            fill = reader.read(readBuffer, 0, readBuffer.length);
            nextPos = 0;

            if (fill == -1) {
                current = -1;
                nextPos++;
                return;
            }
        }

        current = readBuffer[nextPos++];
        column++;

        if (current == NEW_LINE) {
            line++;
            column = 0;
        }
    }

    protected boolean isWhitespace() {
        return current == WHITE_SPACE || current == NEW_LINE || current == LINE_FEED || current == TAB;
    }

    protected void openValueBuffer() {
        if (valueBuffer == null) {
            valueBuffer = new StringBuilder();
        }

        valueStartPos = nextPos - 1;
    }

    protected String closeValueBuffer() {
        if (valueBuffer.length() > 0) {
            valueBuffer.append(readBuffer, valueStartPos, nextPos - valueStartPos - 1);

            String value = valueBuffer.toString();
            valueBuffer.setLength(0);
            valueStartPos = -1;
            return value;
        }

        String val = new String(readBuffer, valueStartPos, nextPos - valueStartPos - 1);
        valueStartPos = -1;
        return val;
    }
}
