import { AUTH_LOGIN, AUTH_LOGOUT, AUTH_ERROR, AUTH_CHECK } from 'react-admin';
import { UserManager } from 'oidc-client';



const issuer = 'https://dev-7w3c59hg.auth0.com';
const clientId = "p4eef1RSRzxUyxlvJDbyQB3T2dg2tMRG" //process.env.REACT_APP_CLIENT_ID;
const redirectUri = "http://localhost:3000/" //process.env.REACT_APP_REDIRECT_URI;

const getProfileFromToken = (tokenJson) => {
    const token = JSON.parse(tokenJson);
    const jwt = JSON.parse(window.atob(token.id_token.split('.')[1]));
    return { id: 'my-profile', ...jwt }
}

const userManager = new UserManager({
    authority: issuer,
    client_id: clientId,
    client_secret: '',
    redirect_uri: redirectUri,
    response_type: 'code',
    scope: 'openid email profile', // Allow to retrieve the email and user name later api side
    filterProtocolClaims: true,
    loadUserInfo: true
});

const cleanup = () => {
    // Remove the ?code&state from the URL
    window.history.replaceState(
        {},
        window.document.title,
        window.location.origin
    );
}

const authProvider = async (type, params = {}) => {
    if (type === AUTH_LOGIN) {
        // 1. Redirect to the issuer to ask authentication
        if (!params.code || !params.state) {
            userManager.signinRedirect();
            return; // Do not return anything, the login is still loading
        }

        const token = await userManager.signinRedirectCallback();
        localStorage.setItem('token', JSON.stringify(token));
        userManager.clearStaleState();
        cleanup();
        return Promise.resolve();
    }

    if ([AUTH_LOGOUT, AUTH_ERROR].includes(type)) {
        localStorage.removeItem('token');
        window.location = issuer + "/v2/logout?returnTo=" + encodeURIComponent(redirectUri) + "&client_id=" + clientId;
        return Promise.resolve();
    }

    if (type === AUTH_CHECK) {
        const token = localStorage.getItem('token');

        if (!token) {
            return Promise.reject()
        }

        // This is specific to the Google authentication implementation
        const jwt = getProfileFromToken(token);
        const now = new Date();

        return now.getTime() > (jwt.exp * 1000) ? Promise.reject() : Promise.resolve()
    }

    return Promise.resolve();
}

export default authProvider;