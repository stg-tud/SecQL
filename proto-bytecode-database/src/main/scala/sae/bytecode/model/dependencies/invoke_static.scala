package sae.bytecode.model.dependencies

import sae.bytecode.model.Method

case class invoke_static(source : Method, target : Method)
        extends calls