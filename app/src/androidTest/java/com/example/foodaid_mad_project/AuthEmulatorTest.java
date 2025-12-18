package com.example.foodaid_mad_project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

/**
 * <h1>AuthEmulatorTest</h1>
 * <p>
 * This class is designed to run Instrumentation Tests (Android Tests) against
 * the local Firebase Authentication Emulator.
 * </p>
 * <ul>
 * <li>It verifies that users can be created and signed in correctly.</li>
 * <li>It ensures invalid credentials (like wrong passwords) are rejected.</li>
 * <li>The tests connect to <b>10.0.2.2:9099</b>, which is the Android
 * Emulator's loopback address to the host machine.</li>
 * </ul>
 */
@RunWith(AndroidJUnit4.class)
public class AuthEmulatorTest {

    private FirebaseAuth mAuth;

    /**
     * <h2>setUp()</h2>
     * <p>
     * Initializes the {@link FirebaseAuth} instance and connects it to the local
     * emulator.
     * </p>
     * <b>Steps:</b>
     * <ol>
     * <li>Get FirebaseAuth instance.</li>
     * <li>Connect to Emulator at 10.0.2.2:9099.</li>
     * <li>Sign out any existing user to ensure a clean state for each test.</li>
     * </ol>
     */
    @Before
    public void setUp() {
        // 1. Get the instance
        mAuth = FirebaseAuth.getInstance();

        // 2. CONNECT TO EMULATOR
        // 10.0.2.2 is the special IP to access your computer's localhost from the
        // Android Emulator
        // 9099 is the default port for Firebase Auth Emulator
        try {
            mAuth.useEmulator("10.0.2.2", 9099);
        } catch (IllegalStateException e) {
            // This exception is expected if the emulator is already connected
            // from a previous test run in the same process. We ignore it.
        }

        // 3. Ensure we start clean (sign out any leftover users)
        mAuth.signOut();
    }

    /**
     * <h2>tearDown()</h2>
     * <p>
     * cleans up the test environment by deleting the test user created during the
     * test.
     * </p>
     * <b>Why?</b> To prevent "state leakage" where a user from Test A interferes
     * with Test B.
     */
    @After
    public void tearDown() {
        if (mAuth.getCurrentUser() != null) {
            try {
                // Delete the current user to keep the emulator clean
                Tasks.await(mAuth.getCurrentUser().delete());
            } catch (ExecutionException | InterruptedException e) {
                // Ignore errors during cleanup, as they shouldn't fail the test
            }
        }
    }

    /**
     * <h2>testCreateUserAndSignIn</h2>
     * <p>
     * Verifies that a valid email and password can successfully create a new
     * account.
     * </p>
     * <b>Logic:</b>
     * <ol>
     * <li>Call {@code createUserWithEmailAndPassword}.</li>
     * <li>Use {@code Tasks.await} to wait for the async operation to complete
     * synchronously.</li>
     * <li>Assert that the result is not null and the email matches.</li>
     * </ol>
     */
    @Test
    public void testCreateUserAndSignIn() {
        String testEmail = "testuser@example.com";
        String testPass = "password123";

        // Step 1: Create User
        try {
            Task<AuthResult> task = mAuth.createUserWithEmailAndPassword(testEmail, testPass);
            AuthResult result = Tasks.await(task);

            // Assertions
            assertNotNull("Auth result should not be null", result);
            assertNotNull("User object inside result should not be null", result.getUser());
            assertEquals("Registered email should match input", testEmail, result.getUser().getEmail());

        } catch (ExecutionException | InterruptedException e) {
            fail("User creation failed: " + e.getMessage());
        }

        // Step 2: Verify Persistence (User is signed in)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        assertNotNull("Current user should be signed in after registration", currentUser);
    }

    /**
     * <h2>testSignInWithWrongPasswordFails</h2>
     * <p>
     * Verifies that the system correctly rejects a login attempt with an incorrect
     * password.
     * </p>
     * <b>Logic:</b>
     * <ol>
     * <li>Create a valid user.</li>
     * <li>Sign out immediately.</li>
     * <li>Attempt to sign in with the SAME email but WRONG password.</li>
     * <li>Expect an exception (Authentication Failure).</li>
     * </ol>
     */
    @Test
    public void testSignInWithWrongPasswordFails() {
        String testEmail = "failuser@example.com";
        String correctPass = "password123";
        String wrongPass = "wrongpass";

        try {
            // Step 1: Create the user first
            Tasks.await(mAuth.createUserWithEmailAndPassword(testEmail, correctPass));

            // Step 2: Sign out
            mAuth.signOut();

            // Step 3: Attempt sign in with WRONG password
            Task<AuthResult> loginTask = mAuth.signInWithEmailAndPassword(testEmail, wrongPass);

            // Step 4: Await result and EXPECT failure
            try {
                Tasks.await(loginTask);
                fail("Login should have failed! The password was incorrect.");
            } catch (ExecutionException e) {
                // Success: We caught the expected exception
                assertNotNull("Exception should be present on auth failure", e);
            }

        } catch (Exception e) {
            fail("Test setup failed (User creation or sign out): " + e.getMessage());
        }
    }

    /**
     * <h2>testCreateUserWithStrongPassword</h2>
     * <p>
     * Verifies that a user can be created with a complex password containing
     * special characters.
     * </p>
     */
    @Test
    public void testCreateUserWithStrongPassword() {
        String testEmail = "strongpass@example.com";
        // Strong password: 8+ chars, uppercase, lowercase, digit, special char
        String strongPass = "StrongP@ss1";

        try {
            // Step 1: Create User
            Task<AuthResult> task = mAuth.createUserWithEmailAndPassword(testEmail, strongPass);
            AuthResult result = Tasks.await(task);

            // Step 2: Assertions
            assertNotNull("Auth result should not be null", result);
            assertNotNull("User should not be null", result.getUser());
            assertEquals("Email should match", testEmail, result.getUser().getEmail());

        } catch (ExecutionException | InterruptedException e) {
            fail("User creation with strong password failed: " + e.getMessage());
        }
    }
}
