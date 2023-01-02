package com.ezdev.sfy;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.ezdev.sfy.dto.AdminDTO;
import com.ezdev.sfy.dto.AdminTempDTO;
import com.ezdev.sfy.dto.MemberDTO;
import com.ezdev.sfy.dto.QnaDTO;
import com.ezdev.sfy.service.AWSs3Mapper;
import com.ezdev.sfy.service.AdminMapper;
import com.ezdev.sfy.service.AdminTempMapper;
import com.ezdev.sfy.service.MemberMapper;
import com.ezdev.sfy.service.QnaMapper;
// @Controller -> Url, @Service ->처리, @Repository -> dao, @Component -> 구성, @RestController -> url과 ajax
//12.05재수정입니다..
@Controller
public class AdminController {
	
	@Autowired
	AdminMapper adminMapper;
	
	@Autowired
	MemberMapper memberMapper;

	@Autowired	
	QnaMapper qnaMapper;
	
	@Autowired
	AdminTempMapper adminTempMapper;
	@Autowired
	AWSs3Mapper s3;
	

	
	//top.jsp 로고 -> 관리자페이지
	@RequestMapping("/admin.do")
	public String admin(HttpSession session, HttpServletRequest req) {
		
		AdminDTO dto = (AdminDTO)session.getAttribute("adto");
		String profile_img = dto.getAdmin_profileImg();
		int admin_no = dto.getAdmin_no();
		String admin_name = dto.getAdmin_name();
		String admin_id = dto.getAdmin_id();
		String admin_passwd = dto.getAdmin_passwd();
		String admin_email = dto.getAdmin_email();
		session.setAttribute("admin_no", admin_no);
		session.setAttribute("profile_img", profile_img); 
		session.setAttribute("admin_id", admin_id);
		session.setAttribute("admin_name", admin_name);
		session.setAttribute("admin_passwd", admin_passwd);
		session.setAttribute("admin_email", admin_email);
		
		// 1. 2주간 회원가입자 수 집계
		int[] memberChartArr = adminMapper.countMemberByWeek();
		session.setAttribute("memberChartValue", memberChartArr);
		
		// 2. 요일별 리뷰갯수가 들어갈 배열
		int[] reviewChartArr = adminMapper.countReviewByWeek();
		session.setAttribute("reviewChartValue", reviewChartArr);

		// 3. 루트테마별 갯수가 들어갈 배열
		int[] routeChartArr = new int[5];
		// 루트테마 이름이 들어간 배열
		String[] routeHashArr = new String[] {"힐링", "미식", "한류", "명소", "쇼핑"};
		
		// 루트테마 이름으로 갯수를 세서 배열에 넣기
		for(int i=0; i<routeChartArr.length; i++) {
			// 최종 넘겨줄 map
			Map<String, String> map = new HashMap<>();
			map.put("hashtag", routeHashArr[i]);
			int num = adminMapper.countRouteByHash(map);
			routeChartArr[i] = num;
		}
		session.setAttribute("routeChartValue", routeChartArr);
		
		List<MemberDTO> list = memberMapper.listOfMember();
		req.setAttribute("listOfMember", list);
		
		return "admin/index";
	}
	
	@RequestMapping("/charts.do")
	public String charts() {
		return "admin/charts";
	} 
	
	@RequestMapping("/table_member.do")
	public String tableMember(HttpServletRequest req) {
		List<MemberDTO> list = memberMapper.listOfMember();
		req.setAttribute("listMember2", list);
		
		
		return "admin/table_member";
	}
	@RequestMapping("/member_update.do")
	public String updateMember(HttpServletRequest req, @ModelAttribute MemberDTO dto) {
		int res = memberMapper.updateListMember(dto);
		 
		if(res>0) {
			req.setAttribute("msg", "회원 정보 수정완료");
			req.setAttribute("url", "table_member.do");
			
		}else{
			req.setAttribute("msg", "회원 정보 수정실패");
			req.setAttribute("url", "table_member.do");
		}
		return "message";
	}
	@RequestMapping("/member_delete.do")
	public String deleteMember(HttpServletRequest req, @ModelAttribute MemberDTO dto, BindingResult result) {
		int res = memberMapper.deleteListMember(dto);
		
		if(res>0) {
			req.setAttribute("msg", "회원 정보 삭제완료");
			req.setAttribute("url", "table_member.do");
			
		}else{
			req.setAttribute("msg", "회원 정보 삭제실패");
			req.setAttribute("url", "table_member.do");
		}
		return "message";
		
	}
	
	@RequestMapping("/table_qna.do")
	public String qnaList(HttpServletRequest req) {
		List<QnaDTO> qlist = qnaMapper.listOfQna();
		req.setAttribute("listOfQna", qlist);
		
		return "admin/table_qna";
	}
	
	@RequestMapping("/fileUpload_ok.do")
	public String fileUploadOk() {
		return "admin/index";
	}
	
	//파일 업로드에 대해 폴더에 저장   
	@RequestMapping("/admin_input_ok.do")
	public String adminInput(HttpServletRequest req, @ModelAttribute AdminDTO dto, BindingResult result) throws Exception{

		if(result.hasErrors()) {
			dto.setAdmin_profileImg("");
		}
		
		MultipartHttpServletRequest mr = (MultipartHttpServletRequest)req;
		MultipartFile files = mr.getFile("admin_profileImg");
		String filename = files.getOriginalFilename();
		
		if(filename == null || filename.trim().equals("")) {
			req.setAttribute("msg", "이미지를 첨부해주세요");
			req.setAttribute("url", "fileUpload_ok.do");
		}
		HttpSession session = req.getSession();
		File target = new File(filename);
		try {
			files.transferTo(target);
		}catch(IOException e) {
			req.setAttribute("msg", "이미지 업로드 실패");
			req.setAttribute("url", "fileUpload_ok.do");
		}
		s3.uploadFile("admin/"+filename, target);
		
		dto.setAdmin_profileImg(filename); 
		dto.setAdmin_name(req.getParameter("admin_name"));
		dto.setAdmin_id(req.getParameter("admin_id"));
		dto.setAdmin_passwd(req.getParameter("admin_passwd"));
		dto.setAdmin_email(req.getParameter("admin_email"));
		
		
		session.setAttribute("adto", dto); //열린 세션에 adto라는 이름에 위의 관리자계정 등록 값dto를 저장
		
		int res = adminMapper.insertAdmin(dto); 
		if(res>0) {
		req.setAttribute("msg","관리자계정 등록성공, 사진 변경완료");
		req.setAttribute("url", "fileUpload_ok.do");
		
		}else {
		req.setAttribute("msg","관리자계정 등록실패");
		req.setAttribute("url", "fileUpload_ok.do");
		 }
		return "message";
		}
	
	@RequestMapping("/admin_temp.do")
	public String upsertAdminTemp(HttpServletRequest req, @ModelAttribute AdminTempDTO dto)  {
		
		int res = adminTempMapper.upsertAdminTemp(dto);
		
		if(res>0) {
			req.setAttribute("msg","임시저장 완료");
			req.setAttribute("url", "table_qna.do");
			return "message";
			
			}else {
			req.setAttribute("msg","임시저장 실패");
			req.setAttribute("url", "table_qna.do");
			return "message";
			 }
		
	}
	@ResponseBody
	@RequestMapping(value="/tempList.do",produces = "application/text; charset=utf8")
	public String tempList(HttpServletRequest req) {

	int qno = Integer.parseInt(req.getParameter("temp_qno"));
	
	AdminTempDTO dto = adminTempMapper.getContent(qno);
	
	String qna_reply_content = dto.getQna_reply_content();
	
	
	return qna_reply_content;
	}
	
	@RequestMapping("/admin_temp_ok.do")
	public String tempOk(HttpServletRequest req, @ModelAttribute QnaDTO dto) {
		int res = adminTempMapper.tempOk(dto);
		
		if(res>0) {
			req.setAttribute("msg", "회원문의함 전송 완료");
			req.setAttribute("url", "table_qna.do");
			return "message";
		}else {
			req.setAttribute("msg", "회원문의함 전송 실패");
			req.setAttribute("url", "table_qna.do");
			return "message";
		}
	}
	
	@RequestMapping("/admin_replyContent_delete.do")
	public String adminReplyContentDelete(HttpServletRequest req, @ModelAttribute QnaDTO dto) {
		int res = adminTempMapper.adminReplyContentDelete(dto);
		
		if(res>0) {
			req.setAttribute("msg", "답변완료 성공");
			req.setAttribute("url", "table_qna.do");
			return "message";
		}else {
			req.setAttribute("msg", "답변완료 실패");
			req.setAttribute("url", "table_qna.do");
			return "message";
		}
		
	}
	
	
	@RequestMapping(value="/admin_login_ok.do", method= RequestMethod.POST)
	public String adminLoginOk(HttpServletRequest req, HttpServletResponse resp, 
	@RequestParam Map<String,String> map) {
		
	AdminDTO dto = adminMapper.getAdminId(map.get("admin_id"));
	
	if(dto == null) {
		req.setAttribute("msg", "해당하는 관리자 계정이 없습니다");
		req.setAttribute("url", "index.do");
	}else { 
		if(dto.getAdmin_passwd().equals(map.get("admin_passwd"))) {
			req.setAttribute("msg", dto.getAdmin_id() + "님 환영합니다");
			req.setAttribute("url", "index.do");

			//세션에 저장(해당 로그인 정보dto를 세션에 저장하여 로그인상태가 유지되도록 함)
			HttpSession session = req.getSession();
			session.setAttribute("adto", dto);
			
			//쿠키전송, 아이디를 기억하기 위해 
			Cookie cookie = new Cookie("saveAid", map.get("admin_id"));
			if(map.containsKey("saveAid")) cookie.setMaxAge(24*60*60);
			else cookie.setMaxAge(0);
			resp.addCookie(cookie);
		}else {
			req.setAttribute("msg", "비밀번호를 잘못눌렀습니다");
			req.setAttribute("url", "index.do");
		}
	}
	return "message";
	}

	@RequestMapping("/adminLogout.do")
	public String adminLogout(HttpServletRequest req) {
		HttpSession session = req.getSession();
		session.invalidate();
		req.setAttribute("msg", "관리자 로그아웃 되었습니다");
		req.setAttribute("url", "index.do");
		return "message";
	}
	
	@RequestMapping("/profile.do")
	public String profile(HttpServletRequest req) {
		HttpSession session = req.getSession();
		String admin_id = (String)session.getAttribute("admin_id");
		AdminDTO dto = adminMapper.getAdminId(admin_id);
		session.setAttribute("getAdmin", dto);
	return "admin/profile";
	}
	
	@RequestMapping("/admin_update.do")
	public String adminUpdate(HttpServletRequest req, @ModelAttribute AdminDTO dto,  BindingResult result) throws Exception{
		HttpSession session = req.getSession();
		String admin_id = (String)session.getAttribute("admin_id");
		AdminDTO adto = adminMapper.getAdminId(admin_id);
		
		if(result.hasErrors()) {
			dto.setAdmin_profileImg("");
		}
		
		MultipartHttpServletRequest mr = (MultipartHttpServletRequest) req;
		MultipartFile file = mr.getFile("admin_profileImg");
		String profile = file.getOriginalFilename();
	
		if (file == null || profile.trim().equals("")) {
			dto.setAdmin_profileImg(adto.getAdmin_profileImg());
		} else {
			File target = new File(profile);
			try {
				file.transferTo(target);
			} catch (IOException e) {
				e.printStackTrace();
				req.setAttribute("msg", "이미지 업로드 중 오류 발생, 다시 등록해주세요");
				req.setAttribute("url", "admin.do");
				return "message";
			}
			s3.deleteFile("admin/"+adto.getAdmin_profileImg());
			s3.uploadFile("admin/"+profile, target);
			
			dto.setAdmin_profileImg(profile);
		}
		
		dto.setAdmin_name(req.getParameter("admin_name"));
		dto.setAdmin_id(req.getParameter("admin_id"));
		dto.setAdmin_passwd(req.getParameter("admin_passwd"));
		dto.setAdmin_email(req.getParameter("admin_email"));
		
		//열린 세션에 adto라는 이름에 위의 관리자계정 등록 값dto를 저장
		session.setAttribute("adto", dto);
		
		int res = adminMapper.adminUpdate(dto);
		
		if(res>0) {
			req.setAttribute("msg", "관리자 정보 수정 완료");
			req.setAttribute("url", "index.do");
		}else {
			req.setAttribute("msg", "관리자 정보 수정 실패");
			req.setAttribute("url", "index.do");
		}
		return "message";
	}
	@RequestMapping("/admin_delete.do")
	public String adminDelete(HttpServletRequest req, @ModelAttribute AdminDTO dto, BindingResult result) {
		int res = adminMapper.adminDelete(dto);
		
		if(res>0) {
			req.setAttribute("msg", "관리자 정보 삭제 완료");
			req.setAttribute("url", "adminLogout.do");
		}else {
			req.setAttribute("msg", "관리자 정보 삭제 실패");
			req.setAttribute("url", "index.do");
		}
		return "message";
		
	}
	
	@ResponseBody
	@RequestMapping(value="/memberEmail.do", produces = "application/text; charset=utf8" )
	public String memberEmail(HttpServletRequest req) {
		

		String member_id = (String)req.getParameter("qna_writer");
		
		MemberDTO dto = memberMapper.getMemberId(member_id);
		
		String member_email = dto.getMember_email();
		
		return member_email;
	}
	}


	

