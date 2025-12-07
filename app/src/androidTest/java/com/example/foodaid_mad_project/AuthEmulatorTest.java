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

@RunWith(AndroidJUnit4.class)
public class AuthEmulatorTest {

    private FirebaseAuth mAuth;

    @Before
    public void setUp() {
        // 1. Get the instance
        mAuth = FirebaseAuth.getInstance();

        // 2. CONNECT TO EMULATOR
        // 10.0.2.2 is the special IP to access your computer's localhost from the Android Emulator
        // 9099 is the default port for Firebase Auth Emulator
        try {
            mAuth.useEmulator("10.0.2.2", 9099);
        } catch (IllegalStateException e) {
            // This prevents a crash if the emulator is already connected
            // from a previous test run in the same process
        }

        // 3. Ensure we start clean (sign out any leftover users)
        mAuth.signOut();
    }

    @After
    public void tearDown() {
        // Clean up after tests to prevent state leakage
        if (mAuth.getCurrentUser() != null) {
            try {
                Tasks.await(mAuth.getCurrentUser().delete());
            } catch (ExecutionException | InterruptedException e) {
                // Log or handle the exception as needed; for now, we ignore to avoid test interruption
            }
        }
    }

    @Test
    public void testCreateUserAndSignIn() {
        String testEmail = "testuser@example.com";
        String testPass = "password123";

        // 1. Create User
        // Note: Firebase calls are asynchronous. In tests, we use Tasks.await
        // to force them to be synchronous so we can assert the results immediately.
        try {
            Task<AuthResult> task = mAuth.createUserWithEmailAndPassword(testEmail, testPass);
            AuthResult result = Tasks.await(task);

            // Assert creation was successful
            assertNotNull("Auth result should not be null", result);
            assertNotNull("User should not be null", result.getUser());
            assertEquals("Email should match", testEmail, result.getUser().getEmail());

        } catch (ExecutionException | InterruptedException e) {
            fail("User creation failed: " + e.getMessage());
        }

        // 2. Verify user is actually signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        assertNotNull("Current user should be signed in", currentUser);
    }

    @Test
    public void testSignInWithWrongPasswordFails() {
        String testEmail = "failuser@example.com";
        String correctPass = "password123";
        String wrongPass = "wrongpass";

        try {
            // Create the user first
            Tasks.await(mAuth.createUserWithEmailAndPassword(testEmail, correctPass));

            // Sign out
            mAuth.signOut();

            // Attempt sign in with WRONG password
            Task<AuthResult> loginTask = mAuth.signInWithEmailAndPassword(testEmail, wrongPass);

            // We expect this to fail
            try {
                Tasks.await(loginTask);
                fail("Login should have failed with wrong password");
            } catch (ExecutionException e) {
                // Determine if the error confirms bad credentials
                // In a real scenario, you might check the specific exception type
                assertNotNull(e);
            }

        } catch (Exception e) {
            fail("Setup for failure test failed: " + e.getMessage());
        }
    }
}
