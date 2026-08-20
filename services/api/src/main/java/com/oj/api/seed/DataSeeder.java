package com.oj.api.seed;

import com.oj.api.repository.ProblemRepository;
import com.oj.api.repository.TestCaseRepository;
import com.oj.api.repository.UserRepository;
import com.oj.common.entity.Problem;
import com.oj.common.entity.TestCase;
import com.oj.common.entity.User;
import com.oj.common.enums.Difficulty;
import com.oj.common.enums.Role;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            UserRepository userRepository,
            ProblemRepository problemRepository,
            TestCaseRepository testCaseRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        userRepository.save(User.builder()
                .username("admin")
                .email("admin@oj.local")
                .passwordHash(passwordEncoder.encode("admin123"))
                .fullName("Admin")
                .role(Role.ADMIN)
                .build());

        userRepository.save(User.builder()
                .username("ashmit")
                .email("ashmit@oj.local")
                .passwordHash(passwordEncoder.encode("ashmit123"))
                .fullName("Ashmit")
                .role(Role.USER)
                .build());

        seedTwoSum();
        seedAddTwoNumbers();
        seedPalindrome();
        seedFizzBuzz();
        seedMaxOfThree();
    }

    private void seedTwoSum() {
        Problem problem = problemRepository.save(Problem.builder()
                .title("Two Sum")
                .slug("two-sum")
                .statement("""
                        Given an array of integers nums and an integer target, return indices of the two numbers
                        such that they add up to target.

                        Input format:
                        First line: n target
                        Second line: n integers

                        Output format:
                        Two indices separated by space (any valid pair).
                        """)
                .difficulty(Difficulty.EASY)
                .tags(List.of("array", "hashmap"))
                .timeLimitMs(2000)
                .memoryLimitMb(256)
                .build());

        saveCases(problem,
                sample("4 9\n2 7 11 15\n", "0 1\n"),
                sample("3 6\n3 2 4\n", "1 2\n"),
                hidden("2 6\n3 3\n", "0 1\n"),
                hidden("5 8\n1 2 3 4 5\n", "2 4\n")
        );
    }

    private void seedAddTwoNumbers() {
        Problem problem = problemRepository.save(Problem.builder()
                .title("A + B")
                .slug("a-plus-b")
                .statement("""
                        Read two integers A and B and print their sum.

                        Input:
                        A B

                        Output:
                        A+B
                        """)
                .difficulty(Difficulty.EASY)
                .tags(List.of("math", "beginner"))
                .timeLimitMs(1000)
                .memoryLimitMb(128)
                .build());

        saveCases(problem,
                sample("1 2\n", "3\n"),
                sample("10 -3\n", "7\n"),
                hidden("100 200\n", "300\n"),
                hidden("-5 -7\n", "-12\n")
        );
    }

    private void seedPalindrome() {
        Problem problem = problemRepository.save(Problem.builder()
                .title("Palindrome Check")
                .slug("palindrome-check")
                .statement("""
                        Given a string S, print YES if it is a palindrome, otherwise NO.
                        Comparison is case-sensitive and ignores no characters.

                        Input:
                        S

                        Output:
                        YES or NO
                        """)
                .difficulty(Difficulty.EASY)
                .tags(List.of("string", "two-pointers"))
                .timeLimitMs(1000)
                .memoryLimitMb(128)
                .build());

        saveCases(problem,
                sample("aba\n", "YES\n"),
                sample("abc\n", "NO\n"),
                hidden("racecar\n", "YES\n"),
                hidden("RaceCar\n", "NO\n")
        );
    }

    private void seedFizzBuzz() {
        Problem problem = problemRepository.save(Problem.builder()
                .title("FizzBuzz")
                .slug("fizzbuzz")
                .statement("""
                        Given an integer N, print numbers from 1 to N.
                        For multiples of 3 print Fizz, for multiples of 5 print Buzz,
                        for multiples of both print FizzBuzz. Otherwise print the number.
                        Print each value on its own line.

                        Input:
                        N

                        Output:
                        N lines
                        """)
                .difficulty(Difficulty.MEDIUM)
                .tags(List.of("math", "implementation"))
                .timeLimitMs(2000)
                .memoryLimitMb(128)
                .build());

        saveCases(problem,
                sample("5\n", "1\n2\nFizz\n4\nBuzz\n"),
                sample("15\n", "1\n2\nFizz\n4\nBuzz\nFizz\n7\n8\nFizz\nBuzz\n11\nFizz\n13\n14\nFizzBuzz\n"),
                hidden("1\n", "1\n"),
                hidden("3\n", "1\n2\nFizz\n")
        );
    }

    private void seedMaxOfThree() {
        Problem problem = problemRepository.save(Problem.builder()
                .title("Max of Three")
                .slug("max-of-three")
                .statement("""
                        Given three integers, print the maximum.

                        Input:
                        A B C

                        Output:
                        max(A, B, C)
                        """)
                .difficulty(Difficulty.EASY)
                .tags(List.of("math", "beginner"))
                .timeLimitMs(1000)
                .memoryLimitMb(64)
                .build());

        saveCases(problem,
                sample("1 2 3\n", "3\n"),
                sample("10 4 7\n", "10\n"),
                hidden("-1 -5 -3\n", "-1\n"),
                hidden("0 0 0\n", "0\n")
        );
    }

    private void saveCases(Problem problem, CaseSpec... specs) {
        for (CaseSpec spec : specs) {
            testCaseRepository.save(TestCase.builder()
                    .problem(problem)
                    .input(spec.input())
                    .expectedOutput(spec.output())
                    .isSample(spec.sample())
                    .build());
        }
    }

    private static CaseSpec sample(String input, String output) {
        return new CaseSpec(input, output, true);
    }

    private static CaseSpec hidden(String input, String output) {
        return new CaseSpec(input, output, false);
    }

    private record CaseSpec(String input, String output, boolean sample) {
    }
}
