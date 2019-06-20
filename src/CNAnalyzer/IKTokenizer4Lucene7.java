package CNAnalyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.IOException;
public final class IKTokenizer4Lucene7 extends Tokenizer {

    // IK分词器实现
    private final IKSegmenter _IKImplement;
    // 词元文本属性
    private final CharTermAttribute termAtt;

    // 词元位移属性
    private final OffsetAttribute offsetAtt;

    // 词元分类属性
    private final TypeAttribute typeAtt;
    // 记录最后一个词元结束位置
    private int endPosition;

    /**
     *
     * @param useSmart
     */
    public IKTokenizer4Lucene7(boolean useSmart){
        super();
        offsetAtt = addAttribute(OffsetAttribute.class);
        termAtt = addAttribute(CharTermAttribute.class);
        typeAtt = addAttribute(TypeAttribute.class);
        _IKImplement = new IKSegmenter(input,useSmart);
    }



    @Override
    public boolean incrementToken() throws IOException {
        // 清除所有词元属性
        clearAttributes();
        Lexeme nextLexeme = _IKImplement.next();
        if (nextLexeme != null){
            // 将Lexeme转成Attributes
            // 设置词元属性
            termAtt.append(nextLexeme.getLexemeText());
            // 设置词元长度
            termAtt.setLength(nextLexeme.getLength());
            // 设置词元唯一
            offsetAtt.setOffset(nextLexeme.getBeginPosition(),nextLexeme.getEndPosition());
            // 记录分词的最后位置
            endPosition = nextLexeme.getEndPosition();
            // 记录词元分类
            typeAtt.setType(nextLexeme.getLexemeTypeString());
            // 返回ture告知还有下个词元
            return  true;
        }
        // 返回false告知输出完毕
        return false;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        _IKImplement.reset(input);
    }

    @Override
    public void end() throws IOException {
        super.end();
        int finalOffset = correctOffset(this.endPosition);
        offsetAtt.setOffset(finalOffset,finalOffset);
    }
}
