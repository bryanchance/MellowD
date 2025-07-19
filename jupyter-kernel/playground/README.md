# MellowD Jupyter Kernel

### dev setup

The install task is setup assuming we are working in the `playground/` folder with a virtual environment setup in `playground/venv/` with jupyter installed there:

```bash
# in playground/
python3 -m venv ./venv
source ./venv/bin/activate
python -m pip install jupyterlab
```

Then run an initial kernel install in that venv. Should see install in `playground/venv/share/jupyter/kernels/mellowd`:
```bash
# in playground/
../../gradlew --project-dir=.. installKernel
```

Then run the kernel, optionally providing a specific soundfont path:
```bash
# in playground/, assuming `source ./venv/bin/activate`
MELLOWD_SF_PATH="FluidR3 GM2-2.SF2" python -m jupyter lab
```

While the server is running you can just rerun the `installKernel` task after making source changes and then in an open notebook, restart the kernel.
