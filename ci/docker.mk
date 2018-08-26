.PHONY: ci_down ci_start ci_stop ci_test ci_up

ci_up: ## Create containers used to run tests on Travis CI
	docker-compose -f .travis/docker-compose-travis.yml up -d

ci_start: ## Start containers stopped by `travis_stop`
	docker-compose -f .travis/docker-compose-travis.yml start

ci_test: ## Run tests on Docker containers, as on Travis CI
	docker exec -it edx_notes_api env TERM=$(TERM) /edx/app/edx_notes_api/edx_notes_api/ci/run_tests.sh

ci_stop: ## Stop running containers created by `travis_up` without removing them
	docker-compose -f .travis/docker-compose-travis.yml stop

ci_down: ## Stop and remove containers and other resources created by `travis_up`
	docker-compose -f .travis/docker-compose-travis.yml down
