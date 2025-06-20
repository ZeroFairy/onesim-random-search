import sys
import subprocess

# This python file is used to run one.bat scripts sequentially and label the runs.

label_placeholder = "NUM"

scripts = [
    ".\\one.bat -b 1 -d Report.reportDir=reports/randomsearch/run/NUM@@MovementModel.rngSeed=0 .\\settings\\demos\\randomsearch-bm.cfg",
    ".\\one.bat -b 1 -d Report.reportDir=reports/randomsearch/run/NUM@@MovementModel.rngSeed=0 .\\settings\\demos\\randomsearch-rw.cfg",
    ".\\one.bat -b 8 -d Report.reportDir=reports/randomsearch/run/NUM@@MovementModel.rngSeed=0 .\\settings\\demos\\randomsearch-lf.cfg",
]

def run_scripts(runs):
    for i in range(runs):
        for script in scripts:
            # Replace both NUM placeholders with the current run number
            script_to_run = script.replace(label_placeholder, str(i + 1))
            print(f"Running: {script_to_run}")

            try:
                # Use subprocess.run with shell=True to handle the batch file execution
                result = subprocess.run(script_to_run, shell=True, check=True,
                                        capture_output=True, text=True)

                # Print output if needed
                print(f"Stdout: {result.stdout}")
                print(f"Stderr: {result.stderr}")

            except subprocess.CalledProcessError as e:
                print(f"Error running script {script_to_run}")
                print(f"Return Code: {e.returncode}")
                print(f"Error Output: {e.stderr}")
                # Optionally, you can choose to break or continue
                # break  # Stop if any script fails
                continue  # Continue with next script if one fails

# run with arg of number of runs
if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python multi-setting-runner.py <number_of_runs>")
        sys.exit(1)

    try:
        number_of_runs = int(sys.argv[1])
        run_scripts(number_of_runs)
    except ValueError:
        print("Please provide a valid integer for the number of runs.")
        sys.exit(1)