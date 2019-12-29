LINK: https://github.com/Kailari/cybersec-project

### FLAW 1: **A1:2017-Injection**
Users are allowed to leave a fun little message when signing. The message is visible to all others who have signed up. The message contents are not properly sanitized, allowing users with malicious intent to inject possibly dangerous HTML to the message.

Normally Thymeleaf would automatically handle this by escaping the HTML tags. However, template has `th:utext` instead of `th:text`, causing the HTML to be passed un-escaped, allowing the injection.

Generally speaking (let's think that thymeleaf does not help us here), easiest fix would be to just strip all HTML-tags from the input string. This, however, might not be 100% fool-proof. One way is to run following algorithm on the string before storing the message:
 1. Replace all `&` characters with `&amp`
 2. Replace all `<` characters with `&lt`
 3. Replace all `>` characters with `&gt`
Step 3. is not strictly necessary, but can be done for symmetry without compromising security (Closing bracket is just another character as long as there is no matching opening bracket). All-in-all this would ensure that any HTML-tags are rendered as text instead of treating them as valid HTML. This avoids loss of data if/when users are trying to be smart with message formatting. 

Furthermore, more refined processing for the messages could be done; for example, some HTML-tags like `<strong>` could be explicitly handled to allow simple message formatting. In case some tags are allowed, care should be taken to not to leak any attack vector from those tags' parameters (e.g. if `<img>`-tags are allowed, "src"-fields should be carefully sanitized to not allow external resources etc.).

### FLAW 2: **A2:2017-Broken Authentication**
The application has hard-coded default users with weak passwords. This allows potential attacker to bypass the authentication and gain access to the list of signed users without requiring registration. In a more complex application, another of the default users might be an admin/root -user with high privileges, allowing the attacker to cause some serious damage.

Another issue is that the application allows non-httpOnly session cookies. This coupled with HTML injection, allows easily reading the JSESSIONID and ultimately performing a session hijack.

The latter is easier to fix. The configuration should be adjusted to enforce JSESSIONID to be httpOnly. This effectively prevents any client-side script in the browser from reading the session cookie, as "If a browser that supports HttpOnly detects a cookie containing the HttpOnly flag, and client side script code attempts to read the cookie, the browser returns an empty string as the result."

### FLAW 3: **A3:2017-Sensitive Data Exposure**
There is some hidden leftover debug HTML on the page where participants are listed. This can be used to display sensitive data of other users. More importantly, this also visualizes that sensitive data is stored with other user information, making it easily identifiable in case of a breach.

First of all, any code that is leaking sensitive user info to public pages should be eliminated. Under no circumstances should personal user information be passed to publicly available pages in a form that anyone with access to browser's developer tools can access.

Furthermore, sensitive information most likely should be stored in a separate data store, which is accessed only when necessary. This would enforce stronger encapsulation of the persona data, making
it harder to make mistakes when dealing with it.

### FLAW 4: **A5:2017-Broken Access Control**
Cancelling the event sign-up does not properly enforce user authentication, allowing any user to remove any other user from the event. This allows malicious users to possibly cause some serious harm to other users, by ultimately preventing them from entering the event. Furthermore, UI-designer has been lazy and the page has "cancel"-links visible for all users, making this sort of "attack" trivial to execute!

The controller responsible for cancelling sign-ups should be made enforce the authentication properly. The controller should check that the user making the request actually is the user being removed, or that the user performing the action has high enough priviledges to do so.

The UI should be changed to reflect the correct behavior. Sign-up cancellation links should be only visible for users who are actually permitted to perform the action. That is, the user should only see the link for themselves and not for the other users.

### FLAW 5: **A9:2017-Using Components with Known Vulnerabilities**
Some libraries the project depends on are older versions. This exposes the application to some serious vulnerabilities. The issue is even more severe due to the possible attackers knowing those vulnerabilities exist. For example, should any of the used libraries contain some known vulnerability which allows compromising an admin user's login credentials or session, the whole system would easily be compromised, no matter how secure the rest of the application is by-design.

This could be easily fixed by updating the library to the newest version and making sure newest versions of libraries are used. For example, to gain additional security against this sort of issues, `Dependency-Check` Maven plugin could be utilized to detect some of these automatically.
