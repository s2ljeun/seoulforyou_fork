<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<head>
	<link rel="stylesheet" href="resources/css/bootstrap.min.css">
	<script src="resources/js/jquery-3.6.1.min.js"></script>
	<script src="resources/js/bootstrap.bundle.min.js"></script>
	<link rel="stylesheet" href="resources/js/bootstrap.min.js">
	  <style>
    body {
      min-height: 100vh;

      background: -webkit-gradient(linear, left bottom, right top, from(#92b5db), to(#1d466c));
      background: -webkit-linear-gradient(bottom left, #92b5db 0%, #1d466c 100%);
      background: -moz-linear-gradient(bottom left, #92b5db 0%, #1d466c 100%);
      background: -o-linear-gradient(bottom left, #92b5db 0%, #1d466c 100%);
      background: linear-gradient(to top right, #92b5db 0%, #1d466c 100%);
    }

    .input-form {
      max-width: 590px;

      margin-top: 80px;
      padding: 32px;

      background: #fff;
      -webkit-border-radius: 10px;
      -moz-border-radius: 10px;
      border-radius: 10px;
      -webkit-box-shadow: 0 8px 20px 0 rgba(0, 0, 0, 0.15);
      -moz-box-shadow: 0 8px 20px 0 rgba(0, 0, 0, 0.15);
      box-shadow: 0 8px 20px 0 rgba(0, 0, 0, 0.15)
    }
  </style>
	<title>회원가입</title>
	
</head>

<body>
<form id="f" name="f" method="post" action="join.do">
  <div class="container">
    <div class="input-form-backgroud row">
      <div class="input-form col-md-12 mx-auto">
        <h4 class="mb-3">회원가입</h4>
          <div class="row">
            <div class="col-md-6 mb-3">
              <label for="name">이름</label>
              <input type="text" class="form-control" name="member_name" id="name" placeholder=""  value="" required="">
              <div class="invalid-feedback">
                	이름을 입력해주세요.
              </div>
            </div>
            <div class="row">
            <div class="col-md-6 mb-3">
              <label for="name">아이디</label>
              <input type="text" class="form-control" name="member_id" id="member_id" placeholder="" status=""  required="">
              <button onclick="checkId()" class="btn btn-outline-secondary">ID 중복확인</button>
              <div class="invalid-feedback">
                	아이디를 입력해주세요.
              </div>
            </div>   
          </div>
          </div>
            <div class="col-lg-8">
          	<label for="password">비밀번호</label>
  			<input type="password" name="member_passwd" id="passwd" class="form-control" placeholder="Password" required="">
	            <div class="invalid-feedback">
	              	비밀번호를 입력해 주세요.
	            </div>
          </div><br>
		<!-- 이메일형식 aaa@aa~ -->
	          <div class="col-lg-8">
	            <label for="email">이메일</label>
	            <input type="email" class="form-control" name="member_email" id="email" placeholder="you@example.com" required="">
	            <div class="invalid-feedback">
	              	이메일을 입력해주세요.
	            </div>
	          </div><br>
	          
				<div class="form-group" id="divPhoneNumber">
                    <label for="inputPhoneNumber" class="col-lg-2 control-label">휴대폰 번호</label>
                    <div class="col-lg-8">
                        <input type="number" class="form-control onlyNumber" name="member_hp" id="phoneNumber" required="" placeholder="-를 제외하고 숫자만 입력하세요." maxlength="11">
                    </div>
                </div><br>
                
          <hr class="mb-4">
 
          <div class="mb-4"></div>
          <button class="btn btn-primary" onclick="submitForm()">가입 완료</button>
          <input class="btn btn-outline-secondary" type="button" style= "width:90px" value="취소" onclick="javascript:go_index()">    
      </div>
    </div>
    <footer class="my-3 text-center text-small">
      <p class="mb-1">&copy; 2022 EzDev</p>
    </footer>
  </div>
</form>
</body>

	<script>
	function go_index(){
		location.href="index.do";
	}
	</script>
	
	<script>
	function submitForm(){
		let status = $('#member_id').attr('status'); //아이디 중복체크 상태
		
		if(status == ""){
			alert("아이디 중복체크를 해주세요.");
			$('#member_id').focus();
		}else if(status == "no"){
			alert("다른 아이디를 입력해주세요.")
			$('#member_id').focus();
		}else{
			$('#f').submit();
		}
	}
	</script>
	
	<script>
	function checkId(){
		let status = $('#member_id').attr('status'); //아이디 중복체크 상태
		let memberId = $('#member_id').val(); //입력한 아이디값
		$('.checkIdSpan').remove(); //기존에 중복체크한 이력 지워주기
		
		//아이디를 입력하지 않았다면
		if(memberId == ""){
			$('#member_id').after("<span class='checkIdSpan' style='color:lightgray'>아이디를 입력해주세요.</span>");
			$('#member_id').focus();
			return
		}
		
		$.ajax({
			url: 'checkId.do',
			type: 'POST',
			async: true,
			data: {
				member_id: memberId
			},
			success: function(data){
				//기존 아이디가 존재한다면
				if(data.cnt > 0){
					$('#member_id').attr('status', 'no');
					$('#member_id').after("<span class='checkIdSpan' style='color:red'>이미 존재하는 아이디입니다.</span>")
					$('#member_id').focus();
					
				//기존 아이디가 존재하지 않으면
				}else{
					$('#member_id').attr('status', 'yes');
					$('#member_id').after("<span class='checkIdSpan' style='color:blue'>사용 가능한 아이디입니다.</span>")
				}
			},
			error: function(e){
				alert("error");
			}
		});
	}
	</script>
