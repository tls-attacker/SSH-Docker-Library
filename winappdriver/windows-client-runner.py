import unittest
import concurrent.futures
import argparse
import time

def run_tests(test_class, methodname, hostname, username, port, debug):
    suite = unittest.TestSuite()
    test = test_class(methodName=methodname, hostname=hostname, username=username, port=port, debug=debug)
    suite.addTest(test)

    # Capture the result
    result = unittest.TextTestRunner(verbosity=2).run(suite)
    return test_class.__name__, result

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Parallel Test Runner')

    # Add common parser arguments
    parser.add_argument('--debug', action='store_true', help='Enable debug printing')
    parser.add_argument('--username', type=str, required=True, help='Username for SSH connection')
    parser.add_argument('--hostname', type=str, required=True, help='Hostname or IP address of the server')

    # Add specific arguments for different test cases
    parser.add_argument('--port-range', type=str, help='Port range in the format start_port-end_port (e.g., 3000-3005) for AbsoluteTelnet .tnt file generation')
    parser.add_argument('--port', type=int, help='Single port for automized TeraTerm CLI')

    args = parser.parse_args()

    # Parse port range if provided
    if args.port_range:
        start_port, end_port = map(int, args.port_range.split('-'))
    elif not args.port:
        parser.error('You must specify either --port-range for test_build_profiles or --port for other tests.')

    hostname = args.hostname
    username = args.username

    from TeraTerm.teratermtest import TeraTermSSHClientTest
    from AbsolutTelnet.generate_absolute_tnt_con_files import AbsolutTelnetSSHClientTest

    function_mapping = {
        AbsolutTelnetSSHClientTest: "test_build_profiles",
        TeraTermSSHClientTest: "test_publickey_auth"
    }

    # Add all your test classes to this list
    test_classes = [AbsolutTelnetSSHClientTest, TeraTermSSHClientTest]

    start_time = time.time()

    with concurrent.futures.ThreadPoolExecutor() as executor:
        futures = []

        for test_class in test_classes:
            methodname = function_mapping.get(test_class)

            if test_class == AbsolutTelnetSSHClientTest and methodname == "test_build_profiles":
                # Ensure port-range is provided for test_build_profiles
                if not args.port_range:
                    parser.error("--port-range must be specified for test_build_profiles.")
                # Iterate over the port range for AbsolutTelnetSSHClientTest
                for port in range(start_port, end_port + 1):
                    futures.append(
                        executor.submit(run_tests, test_class, methodname, hostname, username, port, args.debug)
                    )
            else:
                # Ensure a single port is provided for other tests
                if not args.port:
                    parser.error("--port must be specified for other tests.")
                futures.append(
                    executor.submit(run_tests, test_class, methodname, hostname, username, args.port, args.debug)
                )

        for future in concurrent.futures.as_completed(futures):
            test_class_name, result = future.result()
            if result.wasSuccessful():
                print(f"All repetitions of test suite '{test_class_name}' passed")
            else:
                print(f"Some repetitions of test suite '{test_class_name}' failed")
                print(f"Failures: {len(result.failures)}")
                for failure in result.failures:
                    print(failure)

    end_time = time.time()
    print(f"Total runtime: {end_time - start_time} seconds")
