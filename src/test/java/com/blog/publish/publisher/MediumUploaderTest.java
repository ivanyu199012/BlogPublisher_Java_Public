package com.blog.publish.publisher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.blog.publish.publisher.utils.FileHandler;
import com.blog.publish.publisher.utils.GistCodeHandler;
import com.blog.publish.publisher.utils.MediumUploader;

public class MediumUploaderTest
{
	private static InitialData initialData = null;
	private static final Logger logger = LogManager.getLogger( GistCodeHandlerTest.class );

	@BeforeClass
	public static void setUp() throws IOException, InterruptedException, ClassNotFoundException
	{
		String mediumMarkdownTextPath = "temp\\sample_medium.md";
		String idToGistLinkMapPath = "temp\\idToGistLinkMap.txt";
		if ( ! new File( mediumMarkdownTextPath ).exists() || ! new File( idToGistLinkMapPath ).exists() )
		{
			String markdown = FileHandler.readFile( "temp\\sample.md" );
			Map< String, Object > resultMap = GistCodeHandler.convertBlogCodeToGist( "sample.md", markdown );
			String markdownText = ( String ) resultMap.get( GistCodeHandler.TEMP_MARKDOWN_KEY );
			Map< String, String > idToGistLinkMap = ( Map< String, String > ) resultMap.get( GistCodeHandler.ID_TO_GIST_LINK_MAP_KEY );

			FileHandler.writeObjectToFile( markdownText, mediumMarkdownTextPath );
			FileHandler.writeMapToFile( idToGistLinkMap, idToGistLinkMapPath );
		}
		String markdownText = ( String ) FileHandler.readFileToObject( mediumMarkdownTextPath );
		Map< String, String > idToGistLinkMap = FileHandler.readFileToMap( idToGistLinkMapPath );
		BlogInfo blogInfo = (BlogInfo) FileHandler.readFileToObject( "temp/blogInfo.txt" );
		initialData = new InitialData( markdownText, idToGistLinkMap, blogInfo );
	}

	@Test
	public void test_formatMarkdownText() throws IOException, InterruptedException
	{
		String formattedMarkdownText = MediumUploader.formatMarkdownText(
				initialData.getMarkdownTex(),
				initialData.getIdToGistLinkMap(),
				initialData.getBlogInfo() );
		for ( Map.Entry<String,String> entry : initialData.getIdToGistLinkMap().entrySet() )
		{
			String id = entry.getKey();
			String gistLink = entry.getValue();
			assertTrue( ! formattedMarkdownText.contains( id ) );
			assertTrue( formattedMarkdownText.contains( gistLink ) );
		};
	}

	@Test
	public void test_prepReqDataMap() throws ClassNotFoundException, IOException
	{
		BlogInfo blogInfo = initialData.getBlogInfo();
		String formattedMarkdownText = MediumUploader.formatMarkdownText(
				initialData.getMarkdownTex(),
				initialData.getIdToGistLinkMap(),
				blogInfo );

		Map<String, Object> reqDataMap = MediumUploader.prepReqDataMap( blogInfo, formattedMarkdownText );
		assertEquals( blogInfo.getTitle(), reqDataMap.get( "title" )  );
		assertEquals( formattedMarkdownText, reqDataMap.get( "content" ) );
		assertEquals( blogInfo.getPublishStatus().mediumStatus, reqDataMap.get( "publishStatus" ) );
		assertEquals( blogInfo.getCanonicalUrl(), reqDataMap.get( "canonicalUrl" ) );
	}

	@Ignore
	@Test
	public void test_postArticle() throws IOException, InterruptedException, ClassNotFoundException
	{
		String formattedMarkdownText = MediumUploader.formatMarkdownText(
				initialData.getMarkdownTex(),
				initialData.getIdToGistLinkMap(),
				initialData.getBlogInfo() );

		Map<String, Object> reqDataMap = MediumUploader.prepReqDataMap( initialData.getBlogInfo(), formattedMarkdownText );
		MediumUploader.postArticle( reqDataMap );
	}

	static class InitialData
	{
		String markdownTex;
		Map< String, String > idToGistLinkMap;
		BlogInfo blogInfo;

		public InitialData(String markdownTex, Map< String, String > idToGistLinkMap, BlogInfo blogInfo) {
			super();
			this.markdownTex = markdownTex;
			this.idToGistLinkMap = idToGistLinkMap;
			this.blogInfo = blogInfo;
		}

		public String getMarkdownTex()
		{
			return markdownTex;
		}

		public Map< String, String > getIdToGistLinkMap()
		{
			return idToGistLinkMap;
		}

		public BlogInfo getBlogInfo()
		{
			return blogInfo;
		}
	}
}
