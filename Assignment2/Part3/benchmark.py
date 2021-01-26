import os
import sys
import time
import functools

from threading import Thread
from statistics import mean, variance

"""
    Main `benchmark` decorator.
    When the function decorated by benchmark is invoked, it is executed
    possibly several times (discarding the results) and a small table is
    printed on the standard output including the average time of
    execution and the variance.
"""
def benchmark(warmups=0, iter=1, verbose=False, csv_file=None):
    def decorator(fun):
        # This helper decorator supports introspection on the function name.
        # Without the use of this decorator factory, the name
        # of the decorated functions would have been 'wrapper',
        # and the docstring of the original function would have been lost.
        # Ref: https://docs.python.org/3/library/functools.html
        @functools.wraps(fun)
        def wrapper(*args, **kwargs):
            """
                Helper function that executes the fun function n times and
                keeps track of execution time. Returns all the times as a
                tuple of run information (run num, is warmup, time).
                If the verbose flag is set as true, also prints the information to stdout.
            """
            def start_runs(n, is_warmup, title):
                runs = []
                for i in range(n):
                    t1 = time.perf_counter()
                    fun(*args, **kwargs)
                    t2 = time.perf_counter()
                    execution_time = t2 - t1
                    runs.append((i + 1, is_warmup, execution_time))
                    if verbose:
                        print(f"{title} {i + 1}/{n}: {execution_time:>14.8} s")
                return runs

            # Execute both the warmup runs and the standard executions.
            warmup_runs = start_runs(warmups, True, "Warmup round")
            execution_runs = start_runs(iter, False, "Execution round")

            all_runs = warmup_runs + execution_runs

            # Write the runs to the given csv file.
            if csv_file != None:
                csv_header = ["run num", "is warmup", "timing"]
                with open(csv_file, 'w') as file:
                    file.write(",".join(csv_header) + "\n")
                    for r in all_runs:
                        file.write(",".join(map(str, r)) + "\n")

            # Print a small table on standard output including
            # average time of execution and the variance.
            warmup_times = [t for (_, _, t) in warmup_runs]
            execution_times = [t for (_, _, t) in execution_runs]

            cols = "{:>14}{:>14}{:>14.8}{:>14.8}"
            print(cols.format("is_warmup", "rounds", "mean", "variance"))
            if len(warmup_times) > 1:
                print(cols.format(str(True), len(warmup_times), mean(warmup_times), variance(warmup_times)))
            if len(execution_times) > 1:
                print(cols.format(str(False), len(execution_times), mean(execution_times), variance(execution_times)))

        return wrapper

    return decorator

"""
    Example with the benchmark wrapper and multi-threading.
    It tests the given function f with different threads and total runs,
    while outputting the results of each case on a csv file.
    A number of iterations to run the benchmark can be given to
    repeat the experiments and average out the results.
"""
def test(f, test_iterations):
    def run_threaded(n_threads, n_runs):
        @benchmark(warmups=0, iter=test_iterations, verbose=True, csv_file=f"f_{n_threads}_{n_runs}.csv")
        def experiment():
            # Execute the function the given number of times.
            def repeat_function():
                for _ in range(n_runs):
                    f()
            # Spawn all the required threads for the function.
            threads = [Thread(target=repeat_function) for _ in range(n_threads)]

            # Start the threads,
            for t in threads:
                t.start()
            # and wait for all of them to finish.
            for t in threads:
                t.join()
        return experiment

    # Test the function with the various cases.
    print(f"Testing the function {f.__name__} with 1 thread and 16 runs.")
    run_threaded(n_threads=1, n_runs=16)()
    print(f"Testing the function {f.__name__} with 2 threads and 8 runs.")
    run_threaded(n_threads=2, n_runs=8)()
    print(f"Testing the function {f.__name__} with 4 threads and 4 runs.")
    run_threaded(n_threads=4, n_runs=4)()
    print(f"Testing the function {f.__name__} with 8 threads and 2 runs.")
    run_threaded(n_threads=8, n_runs=2)()

# Standard inefficient recursive un-memoized fibonacci function.
def fib(n=28):
    if n == 0 or n == 1:
        return 1
    else:
        return fib(n - 1) + fib(n - 2)

# Main test function: try the multithreaded benchmark test function on fib.
# The number of iterations can be used to smooth out the results and display an average.
if __name__ == "__main__":
    test(fib, 2)

"""
    Sample results obtained, fib=28, iterations=2 (Intel i7-7500U @ 2.70GHz Quad-Core):

    <f_1_16.csv, n_threads=1, n_runs=16>
    Execution round 1/2:      1.5812959 s
    Execution round 2/2:      1.5538811 s
    is_warmup        rounds          mean      variance
        False             2     1.5675885 0.00037578563

    <f_2_8.csv, n_threads=2, n_runs=8>
    Execution round 1/2:      1.6020445 s
    Execution round 2/2:      1.5394248 s
    is_warmup        rounds          mean      variance
        False             2     1.5707347  0.0019606134

    <f_4_4.csv, n_threads=4, n_runs=4>
    Execution round 1/2:      1.6028507 s
    Execution round 2/2:      1.5867721 s
    is_warmup        rounds          mean      variance
        False             2     1.5948114 0.00012926069

    <f_8_2.csv, n_threads=8, n_runs=2>
    Execution round 1/2:      1.6428870 s
    Execution round 2/2:      1.7709923 s
    is_warmup        rounds          mean      variance
        False             2     1.7069397  0.0082054839

    Comments on the results obtained:

    The execution time does not seem to be affected by increasing the
    number of threads (it even impacts it negatively due to the overhead),
    and all four execution instances essentially take the same amount of time to complete.

    This is because of the GIL (Global Interpreter Lock), which is a mutex built
    in the Python intepreter that globally ensures that only one thread can
    execute Python bytecode (and access Python objects) at a given time.
    The GIL is implemented as a simple solution to deal with multi-threading
    in I/O bound code; however, this has a remarkable effect on CPU-bound code
    (like the fibonacci function defined here, which uses no I/O and simply performs
    calculations), because the GIL will never be released and only a single thread
    at a time will ever be executing the function, thus nullifying the possible
    usefulness of employing multiple threads.

    In our concrete example, if the threads were truly executed in parallel,
    we would expect the running time for 4 runs to be only half to time taken
    for 8 runs, and so on. However, since we need to wait for all the threads
    to finish and only one can run at a time, the function is essentially always
    ran nThreads*nRuns=16 times as if it were executed sequentially.
"""
