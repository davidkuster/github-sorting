package net.talldave.githubsort

import org.junit.Assert
import org.junit.Test
import org.apache.commons.codec.binary.Base64

class AuthServiceTests {

    // TODO: probably need integration test to verify session scoping is working

    @Test
    void "test user login"() {
        AuthService service = new AuthService()

        // login user
        service.login('user', 'pass')

        Assert.assertTrue(service.isLoggedIn())
        Assert.assertEquals('user', service.username)
        Assert.assertEquals('pass', service.password)
        Assert.assertEquals('user', service.getUser())
    }

    @Test
    void "test user logout"() {
        AuthService service = new AuthService()

        // login user
        service.login('user', 'pass')
        Assert.assertTrue(service.isLoggedIn())

        // logout user
        service.logout()

        Assert.assertFalse(service.isLoggedIn())
        Assert.assertNull(service.username)
        Assert.assertNull(service.password)
    }

    @Test
    void "test credentials encoding"() {
        AuthService service = new AuthService()

        // login user
        service.login('user', 'pass')

        // encode credentials
        String encoded = service.encodeCredentials()

        // decode credentials
        // and hmm...the test has to know a bit more about the internals of the class and how the encoding is being done which may indicate a need for a bit of refactoring...
        String decoded = new String(Base64.decodeBase64(encoded))

        Assert.assertEquals("user:pass", decoded)
    }

    @Test
    void "test credentials encoding with a custom separator"() {
        AuthService service = new AuthService()

        // login user
        service.login('user', 'pass')

        // encode credentials
        String encoded = service.encodeCredentials('|||')

        // decode credentials
        String decoded = new String(Base64.decodeBase64(encoded))

        Assert.assertEquals("user|||pass", decoded)
    }

}