run_test_workflow:
	act --workflows .github/workflows/test.yaml -P ubuntu-24.04=catthehacker/ubuntu:act-22.04 --env ACTIONS_RUNTIME_TOKEN=12345
