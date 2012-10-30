package eu.wegov.web.security;

public class TestWegovLoginService extends WegovLoginService {
	public TestWegovLoginService() {
		super();
	}

	public String getLoggedInUser() {
		String userName = "kem";
		System.out.println("TestWegovLoginService: username = " + userName);
		return userName;
	}

}
