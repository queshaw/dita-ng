err = System.err

if (args.length < 2) {
  err.println "Usage: fpi2uri.groovy <config-file> <source-directory>"
  System.exit 1
}

def config_file = args[0]
def dir = args[1]

boolean appropriate_file(f) {
  if (!f.canRead())
    return false
  if (!f.isFile())
    return false
  def name = f.name
  return (name.endsWith('.ditamap')
         || name.endsWith('.dita')
         || name.endsWith('.xml'))
}

cfg = new ConfigSlurper().parse(new File(config_file).text)

def schema_of_doctype(s) {
  return cfg.map*.value.groupBy { it.fpi }."${s}"?.urn?.getAt(0)
}

def fpi_rex = $/(?s)^.*?<!DOCTYPE\s+\S+\s+PUBLIC\s+['"](.*?)['"]\s+['"](.*?)['"]\s*>(.*)/$

new File(dir).eachFileRecurse { f ->
  if (appropriate_file(f)) {
    def fn = f.canonicalPath
    def bak = new File("${fn}.bak")
    if (bak.exists())
      err.println "Not overwritting ${bak}"
    else {
      if (!f.renameTo(bak))
        err.println "Coud not rename ${fn} to ${bak}"
      else {
        println "Updating ${fn}"
        def nf = new File(fn)
        def text = bak.text
        def m = text =~ fpi_rex
        if (!m)
          System.err.println "Didn\'t find a doctype statement in ${nf}"
        else {
          def rng = schema_of_doctype(m.group(1))
          if (!rng)
            System.err.println "Didn\'t find urn for ${m.group(1)}"
          else {
            nf.withWriter('utf-8') { w ->
              def rest = m.group(3)
              w.write("<?xml-model href=\"${rng}\"?>${rest}")
            }
          }
        }
      }
    }
  }
}
