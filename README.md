Ex2Amplifier [![Build Status](https://travis-ci.org/STAMP-project/Ex2Amplifier.svg?branch=master)](https://travis-ci.org/STAMP-project/Ex2Amplifier)[![Coverage Status](https://coveralls.io/repos/github/STAMP-project/Ex2Amplifier/badge.svg?branch=master)](https://coveralls.io/github/STAMP-project/Ex2Amplifier?branch=master)
=====================================================================================================================
![STAMP - European Commission - H2020](docs/logo_readme_md.png)

**Ex2Amplifier** stands for Exhaustive-Explorer-Amplifier.

This tool is an amplifier, that means that it mutated existing test to explore the behavior space.

**Ex2Amplifier** is based on the small scope hypothesis. It instruments the source (business) code. Then executes existing (often manually written) test case.

From this execution it builds an **Alloy** model, by encoding all objects as _signatures_. It also encodes conditional (if then/else) and assignments as _fact_.

At the end, it negates one of constraint to trigger a new behavior, and generate new inputs for the test.