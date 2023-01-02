package com.ezdev.sfy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ezdev.sfy.dto.MemberDTO;
import com.ezdev.sfy.service.KakaoMapper;
import com.ezdev.sfy.service.MemberMapper;

@Controller
public class MemberController {
	@Autowired
	MemberMapper memberMapper;
	@Autowired
	KakaoMapper kakaoMapper;

	//로그인
	@RequestMapping(value="/login_ok.do" , method =RequestMethod.POST)
	public String login_ok(HttpServletRequest req, HttpServletResponse resp, 
			@RequestParam Map<String, String> map) {
		
		
		MemberDTO dto = memberMapper.getMemberId(map.get("member_id"));	
		
		if(dto == null) {
			req.setAttribute("msg", "해당하는 아이디가 없습니다. 다시 확인하시고 입력해 주세요");
			req.setAttribute("url", "index.do");
		} else {
			int member_no = dto.getMember_no();
			String member_id = dto.getMember_id();
			if(dto.getMember_passwd().equals(map.get("member_passwd"))) {
				req.setAttribute("msg", dto.getMember_name()+"님, 환영합니다");
				req.setAttribute("url", "index.do");
				
				//세션에 저장
				HttpSession session = req.getSession();
				session.setAttribute("mdto", dto);
				session.setAttribute("nowUserNo", member_no);
				session.setAttribute("nowUserId", member_id);
				//쿠키 전송
				Cookie cookie = new Cookie("saveId", map.get("member_id"));
				if(map.containsKey("saveId")) cookie.setMaxAge(24*60*60);
				else cookie.setMaxAge(0);
				resp.addCookie(cookie);
			}else {
				req.setAttribute("msg", "비밀번호가 틀렸습니다. 다시 확인하시고 입력해 주세요");
				req.setAttribute("url", "index.do");
			}
		}
		return "message";
	}


		@RequestMapping("/logout.do")
		public String logout(HttpServletRequest req) {
			HttpSession session = req.getSession();
			session.invalidate();
			req.setAttribute("msg", "로그아웃 되었습니다.");
			req.setAttribute("url", "index.do");
			return "message";
		}

	//회원가입
	@RequestMapping(value="/join.do", method=RequestMethod.GET)
	public String member_input() {
		return "pages/join";
	}
	
	@RequestMapping(value="/join.do", method=RequestMethod.POST)
	public String member_input_ok(HttpServletRequest req, @ModelAttribute MemberDTO dto,@RequestParam String member_id) {
		  //아이디 중복 확인을 위한 id값 받아오기
			MemberDTO dto2 = memberMapper.getMemberId(member_id);
		 if(dto2 == null) {
			 //데이터에 id가 없으면 회원가입 진행
			int res = memberMapper.insertMember(dto);
			if (res>0) {// 멤버table insert 성공시
				// 마이페이지table에 insert
				String id = dto.getMember_id();
				MemberDTO mdto = memberMapper.getMemberId(id);
				int res_mypage = memberMapper.insertMypage(mdto);
				
				if(res_mypage>0) {
					req.setAttribute("msg", "회원 등록 성공!! 로그인을 해 주세요");
					req.setAttribute("url", "index.do");
				}else {
					req.setAttribute("msg", "마이페이지 등록 실패!! 관리자에게 문의하세요.");
					req.setAttribute("url", "join.do");
				}
			}else {// 멤버table insert 실패시
				req.setAttribute("msg", "회원 등록 실패!! 회원등록페이지로 이동합니다.");
				req.setAttribute("url", "join.do");
			}
		 } else {
			 //아이디가 존재하면 다시 진행
			 req.setAttribute("msg", "이미 존재하는 아이디입니다");
		 }
		return "message";
	}
	
	//회원가입 - 아이디 중복체크
	@RequestMapping("checkId.do")
	@ResponseBody
	public Map<Object, Object> checkId(@RequestParam String member_id) {
		MemberDTO dto = memberMapper.getMemberId(member_id);
		Map<Object, Object> map = new HashMap<>();

		// 아이디가 존재하지 않으면
		if(dto == null) {
			map.put("cnt", 0);
		// 아이디가 존재하면
		}else {
			map.put("cnt", 1);
		}
		
		return map;
	}
	
	
	//카카오 로그인
		@RequestMapping(value="/kakaoLogin.do", method=RequestMethod.GET)
		public String getInfo(HttpServletRequest req, @RequestParam String code) throws IOException {
	       HttpSession session = req.getSession();
			System.out.println("code = " + code);
	        String access_token = kakaoMapper.getToken(code); 
	        Map<String, String> userInfo = kakaoMapper.getUserInfo(access_token);

	       
	        MemberDTO dto = memberMapper.getMemberEmail(userInfo.get("member_email"));
	        if(dto == null) {
	        	int res = memberMapper.insertMemKakao(userInfo);
	        	if(res>0) {
	        		MemberDTO mdto = memberMapper.getMemberId(userInfo.get("member_id"));
	        		int res_mypage = memberMapper.insertMypage(mdto);
	        		
	        		if(res_mypage>0) {
	        			if(res_mypage>0) {
	        				req.setAttribute("msg", "회원 등록 성공!! 로그인을 해 주세요");
	        				req.setAttribute("url", "index.do");
	        			}else {
	        				req.setAttribute("msg", "마이페이지 등록 실패!! 관리자에게 문의하세요.");
	        				req.setAttribute("url", "join.do");
	        			}
	        	}else {
	        		req.setAttribute("msg", "회원 등록 실패!! 관리자에게 문의하세요.");
	    			req.setAttribute("url", "join.do");
	        	}
	        		return "message";
	        	}
	        }
	        if(userInfo.get("member_email") != null) {
	        	int member_no = dto.getMember_no();
	        	String member_id = dto.getMember_id();
	        	session.setAttribute("mdto", dto);
	        	session.setAttribute("nowUserNo", member_no);
	        	session.setAttribute("nowUserId", member_id);
	        	session.setAttribute("userId", userInfo.get("member_email"));
	        	session.setAttribute("access_token", access_token);
	        }
	        
	        	return "index";
		}     
	     
		@RequestMapping(value="/kakaoLogout.do")
		public String kakaologout(HttpSession session) {
			 kakaoMapper.kakaoLogout((String)session.getAttribute("access_Token"));
			    session.removeAttribute("access_Token");
			    session.removeAttribute("userId");
			    return "index";
		}
		
			
	}