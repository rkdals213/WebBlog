package com.ssafy.webblog.model.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssafy.webblog.model.dto.Likearticle;

public interface LikearticleDao extends JpaRepository<Likearticle, String> {
	//한 게시글의 좋아요 수
	
	List<Likearticle> getLikeByArticleid(int articleid);
	
	List<Likearticle> getLikeByUserid(int userid);
	
	Likearticle getLikeByUseridAndArticleid(int userid, int articleid);
}
