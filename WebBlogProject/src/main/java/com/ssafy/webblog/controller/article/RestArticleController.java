package com.ssafy.webblog.controller.article;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ssafy.webblog.model.dto.Article;
import com.ssafy.webblog.model.dto.Tag;
import com.ssafy.webblog.model.dto.Tagkind;
import com.ssafy.webblog.model.service.ArticleService;
import com.ssafy.webblog.model.service.FileUploadDownloadService;
import com.ssafy.webblog.model.service.FileUploadResponse;
import com.ssafy.webblog.model.service.LikeService;
import com.ssafy.webblog.model.service.TagService;
import com.ssafy.webblog.model.service.TagkindService;
import com.ssafy.webblog.model.service.ThumbnailUploadDownloadService;

import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = { "*" })
@RestController
@RequestMapping("/article")
public class RestArticleController {

	static Logger logger = LoggerFactory.getLogger(RestArticleController.class);

	@Autowired
	ArticleService artiService;
	
	@Autowired
	LikeService lService;

	@Autowired
	TagService tService;
	@Autowired
	TagkindService tkService;

	@GetMapping("/detail/{articleId}")
	@ApiOperation(value = "게시글 조회")
	public ResponseEntity<Map<String, Object>> getArticle(HttpServletRequest req, HttpServletResponse res, @PathVariable int articleId)
			throws JsonProcessingException, IOException {
		logger.debug("select by article id: " + articleId);
		ResponseEntity<Map<String, Object>> entity = null;
		try {
			Article result = artiService.getArticleByArticleId(articleId);
			logger.debug(result.toString());
			entity = handleSuccess(result);
		} catch (RuntimeException e) {
			entity = handleException(e);
		}
		return entity;
	}
	@PostMapping("/regist")
	@ApiOperation(value = "게시글 등록")
	public ResponseEntity<Map<String, Object>> articleRegist(HttpServletRequest req, @RequestBody Article article)
			throws IOException {
		logger.debug("Article regist : " + article.toString());
		ResponseEntity<Map<String, Object>> entity = null;
		try {
			req.getHeader("jwt-auth-token");
			Article result = artiService.insertArticle(article);
			System.out.println(result);
			entity = handleSuccess(result);
		} catch (RuntimeException e) {
			entity = handleException(e);
		}
		return entity;
	}


	@DeleteMapping("/delete/{articleid}")
	@ApiOperation(value = "게시글 삭제")
	public ResponseEntity<Map<String, Object>> articleDelete(HttpServletRequest req, HttpServletResponse res, @PathVariable int articleid)
			throws IOException {
		logger.debug("delete article by articleId: " + articleid);
		ResponseEntity<Map<String, Object>> entity = null;
		try {
			artiService.deleteArticle(articleid);
			List<Tag> deleteTagTarget = tService.getTagListByArticleid(articleid);
			for(Tag tag : deleteTagTarget) {
				tService.deleteTag(tag.getTagid());
				int size = tService.countByTagname(tag.getTagname());
				if(size <= 0) tkService.delete(tag.getTagname());
				else tkService.insertTagkind(new Tagkind(tag.getTagname(), size));
			}
			entity = handleSuccess("success");
		} catch (RuntimeException e) {
			entity = handleException(e);
		}
		return entity;
	}

	@PutMapping("/update")
	@ApiOperation(value = "게시글 수정")
	public ResponseEntity<Map<String, Object>> articleUpdate(HttpServletRequest req, HttpServletResponse res, @RequestBody Article article)
			throws  IOException {
		logger.debug("update article before : " + artiService.getArticleByArticleId(article.getArticleid()));
		logger.debug("update article after : " + article.toString());
		ResponseEntity<Map<String, Object>> entity = null;
		try {
			Article result = artiService.updateArticle(article);
			entity = handleSuccess(result);
		} catch (RuntimeException e) {
			entity = handleException(e);
		}
		return entity;
	}


	@GetMapping("/searchBy/nickname/{nickname}/{page}")
	@ApiOperation(value = "게시글 목록 검색 - 닉네임으로 검색")
	public ResponseEntity<Map<String, Object>> getArticleListByNickname(HttpServletRequest req, HttpServletResponse res, @PathVariable String nickname, @PathVariable int page)
			throws IOException {
		logger.debug("Searching article by nickname : " + nickname);
		ResponseEntity<Map<String, Object>> entity = null;
		try {
			Page<Article> result = artiService.searchBy(nickname, 1, page);
			entity = handleSuccess(result);
		} catch (RuntimeException e) {
			entity = handleException(e);
		}
		return entity;
	}
	@GetMapping("/searchBy/category/{category}/{page}")
	@ApiOperation(value = "게시글 목록 검색 - 카테고리별로 검색")
	public ResponseEntity<Map<String, Object>> getArticleListByCategory(HttpServletRequest req, HttpServletResponse res, @PathVariable String category, @PathVariable int page)
			throws IOException {
		logger.debug("Searching article by nickname : " + category);
		ResponseEntity<Map<String, Object>> entity = null;
		try {
			Page<Article> result = artiService.searchBy(category, 2, page);
			entity = handleSuccess(result);
		} catch (RuntimeException e) {
			entity = handleException(e);
		}
		return entity;
	}
	@GetMapping("/searchBy/title/{title}/{page}")
	@ApiOperation(value = "게시글 목록 검색 - 타이틀 검색")
	public ResponseEntity<Map<String, Object>> getArticleListByTitle(HttpServletRequest req, HttpServletResponse res, @PathVariable String title, @PathVariable int page)
			throws IOException {
		logger.debug("Searching article by title: " + title);
		ResponseEntity<Map<String, Object>> entity = null;
		try {
			Page<Article> result = artiService.searchBy(title, 0, page);
			entity = handleSuccess(result);
		} catch (RuntimeException e) {
			entity = handleException(e);
		}
		return entity;
	}

	@GetMapping("/searchBy/tag/{tagname}/{page}")
	@ApiOperation(value = "태그로 아티클검색")
	public ResponseEntity<Map<String, Object>> getArticleListByTagname(HttpServletRequest req, HttpServletResponse res, @PathVariable String tagname, @PathVariable int page)
			throws JsonProcessingException, IOException {
		logger.debug("Searching article by tagname : " + tagname);
		Map<String, Object> resultMap = new HashMap<>();
		ResponseEntity<Map<String, Object>> entity = null;
		try {
			List<Article> result = tService.getArticleListByTagname(tagname, page);	
			int size = tService.countByTagname(tagname);
			resultMap.put("content", result);
			resultMap.put("totalElements", size);
			entity = handleSuccess(resultMap);
		} catch (RuntimeException e) {
			entity = handleException(e);
		}
		return entity;
	}
	
	@GetMapping("/searchBy/Liked/{userid}/{page}")
	@ApiOperation(value = "좋아요한 아티클검색")
	public ResponseEntity<Map<String, Object>>
	getArticleListByLiked(HttpServletRequest req, HttpServletResponse res, @PathVariable int userid, @PathVariable int page)
			throws JsonProcessingException, IOException {
		logger.debug("Searching liked article by userid : " + userid);
		Map<String, Object> resultMap = new HashMap<>();
		ResponseEntity<Map<String, Object>> entity = null;
		try {
			List<Article> result = artiService.getLikedArticleListByUserId(userid, page);
			int size = lService.countByUserid(userid);
			resultMap.put("content", result);
			resultMap.put("totalElements", size);
			entity = handleSuccess(resultMap);
		} catch (RuntimeException e) {
			entity = handleException(e);
		}
		return entity;
	}



	@GetMapping("/searchBy/allarticle/{sort}/{page}")
	@ApiOperation(value = "전체 게시글 조회")
	public ResponseEntity<Map<String, Object>> getAllArticleList(HttpServletRequest req, HttpServletResponse res, @PathVariable int page, @PathVariable int sort)
			throws IOException {
		logger.debug("Searching all article ");
		ResponseEntity<Map<String, Object>> entity = null;
		try {
			Page<Article> result = artiService.searchAll(sort, page);
			entity = handleSuccess(result);
		} catch (RuntimeException e) {
			entity = handleException(e);
		}
		return entity;
	}


	@GetMapping("/user/{userid}/{page}")
	@ApiOperation(value = "유저가 작성한 게시글 조회")
	public ResponseEntity<Map<String, Object>> getArticleBy(HttpServletRequest req, HttpServletResponse res, @PathVariable String userid, @PathVariable int page)
			throws JsonProcessingException, IOException {
		logger.debug("Articletemp select by user id: " + userid);
		ResponseEntity<Map<String, Object>> entity = null;
		try {
			Page<Article> result = artiService.getArticleListByWriterid(Integer.parseInt(userid), page);
			logger.debug(result.toString());
			entity = handleSuccess(result);
		} catch (RuntimeException e) {
			entity = handleException(e);
		}
		return entity;
	}
	
	@GetMapping("/visit/{articleid}")
	@ApiOperation(value = "게시글 조회 카운트 추가")
	public ResponseEntity<Map<String, Object>> visited(HttpServletRequest req, HttpServletResponse res, @PathVariable String articleid)
			throws IOException {
		logger.debug("Searching all article ");
		ResponseEntity<Map<String, Object>> entity = null;
		try {
			int hits = artiService.visitedArticle(Integer.parseInt(articleid));
			entity = handleSuccess(hits);
		} catch (RuntimeException e) {
			entity = handleException(e);
		}
		return entity;
	}

	@Autowired
	private ThumbnailUploadDownloadService service;

	@PostMapping("/uploadThumbnail")
	public FileUploadResponse uploadFile(HttpServletRequest req, @RequestParam("file") MultipartFile file) {
		String fileName = "";
		try {
			String articleNum = req.getHeader("articleNum");
			fileName = service.storeFile(file, articleNum);
		} catch (FileUploadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/downloadFile/")
				.path(fileName)
				.toUriString();
		
		System.out.println(fileDownloadUri);

		return new FileUploadResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());
	}



	@GetMapping("/downloadThumbnail/{fileName:.+}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) throws MalformedURLException{
		// Load file as Resource
		Resource resource = service.loadFileAsResource(fileName);
		// Try to determine file's content type
		String contentType = null;
		try {
			System.out.println(resource.getFile().getAbsolutePath());
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		} catch (IOException ex) {
			logger.info("Could not determine file type.");
		}

		// Fallback to the default content type if type could not be determined
		if(contentType == null) {
			contentType = "application/octet-stream";
		}

		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}

	private ResponseEntity<Map<String, Object>> handleSuccess(Object data) {
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("status", true);
		resultMap.put("data", data);
		return new ResponseEntity<Map<String, Object>>(resultMap, HttpStatus.OK);
	}

	private ResponseEntity<Map<String, Object>> handleException(Exception e) {
		logger.error("예외 발생 : ", e);
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("status", false);
		resultMap.put("data", e.getMessage());
		return new ResponseEntity<Map<String, Object>>(resultMap, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}