package com.xiaobai.code.lucene;

import com.xiaobai.code.entity.ArcType;
import com.xiaobai.code.entity.Article;
import com.xiaobai.code.entity.User;
import com.xiaobai.code.util.DateUtil;
import com.xiaobai.code.util.StringUtil;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 资源索引类
 */
@Component
public class ArticleIndex {

    private Directory dir = null;

    @Value("${lucenePath}")
    private String lucenePath;

    /**
     * 获取IndexWriter实例
     * @return
     */
    private IndexWriter getWriter() throws IOException {
        dir = FSDirectory.open(Paths.get(lucenePath));
        SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(dir,iwc);
        return writer;
    }
    /**
     * 添加资源索引
     * @param article
     */
    public void addIndex(Article article) {
        ReentrantLock reentrantLock = new ReentrantLock();
        reentrantLock.lock();
        try {
            IndexWriter writer = getWriter();
            Document document = getIndexableFields(article);
            writer.addDocument(document);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            reentrantLock.unlock();
        }
    }

    /**
     * 修改资源索引
     * @param article
     */
    public void updateIndex(Article article) {
        ReentrantLock reentrantLock = new ReentrantLock();
        reentrantLock.lock();
        try {
            IndexWriter writer = getWriter();
            Document document = getIndexableFields(article);
            writer.updateDocument(new Term("id",String.valueOf(article.getArticleId())),document);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            reentrantLock.unlock();
        }
    }

    /**
     * 删除资源索引
     * @param id
     */
    public void deleteIndex(String id) {
        ReentrantLock reentrantLock = new ReentrantLock();
        reentrantLock.lock();
        try {
            IndexWriter writer = getWriter();
            writer.deleteDocuments(new Term("id",id));
            writer.forceMergeDeletes();                     //强制删除
            writer.commit();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            reentrantLock.unlock();
        }
    }

    private Document getIndexableFields(Article article) {
        Document document = new Document();
        document.add(new StringField("id", String.valueOf(article.getArticleId()), Field.Store.YES));
        document.add(new TextField("name", article.getName(), Field.Store.YES));
        document.add(new StringField("publishDate",DateUtil.formatDate(article.getPublishDate(), "yyyy-MM-dd hh mm"), Field.Store.YES));
        document.add(new TextField("content", article.getContent(), Field.Store.YES));
        document.add(new StringField("points", String.valueOf(article.getPoints()), Field.Store.YES));
        document.add(new StringField("click", String.valueOf(article.getClick()), Field.Store.YES));
        document.add(new StringField("arcTypeName", article.getArcType().getArcTypeName(), Field.Store.YES));
        document.add(new StringField("headPortrait", String.valueOf(article.getUser().getHeadPortrait()), Field.Store.YES));
        document.add(new StringField("nickname", String.valueOf(article.getUser().getNickname()), Field.Store.YES));
        document.add(new StringField("isVip", String.valueOf(article.getUser().isVip()), Field.Store.YES));
        document.add(new StringField("vipGrade", String.valueOf(article.getUser().getVipGrade()), Field.Store.YES));
        document.add(new StringField("isFree", String.valueOf(article.isFree()), Field.Store.YES));
        document.add(new StringField("isHot", String.valueOf(article.isHot()), Field.Store.YES));
        return document;
    }

    /**
     * 查询资源无高亮列表
     */
    public List<Article> searchNoHighLightet(String q) throws IOException, ParseException {
        q = q.replaceAll("[^0-9a-zA-Z\u4e00-\u9fa5]","");
        dir = FSDirectory.open(Paths.get(lucenePath));
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher indexSearcher = new IndexSearcher(reader);
        SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();
        QueryParser parser = new QueryParser("name",analyzer);
        Query query = parser.parse(q);
        QueryParser parser2 = new QueryParser("content",analyzer);
        Query query2 = parser.parse(q);
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        booleanQuery.add(query, BooleanClause.Occur.SHOULD);
        booleanQuery.add(query2, BooleanClause.Occur.SHOULD);
        TopDocs hits =  indexSearcher.search(booleanQuery.build(),10);
        List<Article> articleList = new ArrayList<>();
        ScoreDoc[] scoreDocs = hits.scoreDocs;
        for (ScoreDoc scoreDoc:scoreDocs){
            Document doc = indexSearcher.doc(scoreDoc.doc);
            Article article = new Article();
            article.setArticleId(Integer.parseInt(doc.get("id")));
            article.setName(doc.get("name"));
            article.setClick(Integer.parseInt(doc.get("click")));
            articleList.add(article);
        }
        return articleList;
    }

    /**
     * 查询资源详细列表，带高亮
     */
    public List<Article> search(String q) throws IOException, ParseException, InvalidTokenOffsetsException, java.text.ParseException {
        q = q.replaceAll("[^0-9a-zA-Z\u4e00-\u9fa5]","");
        dir = FSDirectory.open(Paths.get(lucenePath));
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher indexSearcher = new IndexSearcher(reader);
        SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();
        QueryParser parser = new QueryParser("name",analyzer);
        Query query = parser.parse(q);
        QueryParser parser2 = new QueryParser("content",analyzer);
        Query query2 = parser.parse(q);
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        booleanQuery.add(query, BooleanClause.Occur.SHOULD);
        booleanQuery.add(query2, BooleanClause.Occur.SHOULD);
        TopDocs hits =  indexSearcher.search(booleanQuery.build(),100);
        List<Article> articleList = new ArrayList<>();
        QueryScorer queryScorer = new QueryScorer(query);
        Fragmenter fragmenter = new SimpleSpanFragmenter(queryScorer);
        SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<b><font color='red'>","</font></b>");
        Highlighter highlighter = new Highlighter(simpleHTMLFormatter,queryScorer);
        highlighter.setTextFragmenter(fragmenter);
        ScoreDoc[] scoreDocs = hits.scoreDocs;
        for (ScoreDoc scoreDoc:scoreDocs){
            Document doc = indexSearcher.doc(scoreDoc.doc);
            Article article = new Article();
            article.setArticleId(Integer.parseInt(doc.get("id")));
           String name = doc.get("name");
            if (name != null) {
                TokenStream tokenStream = analyzer.tokenStream("name",new StringReader(name));
                String hname = highlighter.getBestFragment(tokenStream,name);
                if(StringUtil.isEmpty(hname)){
                    article.setName(name);
                }else{
                    article.setName(hname);
                }
            }
            article.setClick(Integer.parseInt(doc.get("click")));
            article.setPublishDate(DateUtil.formatString(doc.get("publishDate"),"yyyy-MM-dd hh mm"));
            article.setPoints(Integer.parseInt(doc.get("points")));
            article.setFree(Boolean.valueOf(doc.get("isFree")));
            article.setHot(Boolean.valueOf(doc.get("isHot")));
            ArcType arcType = new ArcType();
            arcType.setArcTypeName(doc.get("arcTypeName"));
            article.setArcType(arcType);
            User user = new User();
            user.setHeadPortrait(doc.get("headPortrait"));
            user.setNickname(doc.get("nickname"));
            user.setVip(Boolean.valueOf(doc.get("isVip")));
            user.setVipGrade(Integer.parseInt(doc.get("vipGrade")));
            article.setUser(user);
            articleList.add(article);
        }
        return articleList;
    }
}
