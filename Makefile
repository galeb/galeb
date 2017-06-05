VERSION=4.0.5

deploy-snapshot:
	mvn clean install -DskipTests deploy:deploy -DaltDeploymentRepository=oss-jfrog::default::http://oss.jfrog.org/artifactory/oss-snapshot-local

galeb-next: clean
	mvn package -DskipTests

test:
	mvn test

clean:
	mvn clean
	rm -f galeb-router-${VERSION}-1.el7.noarch.rpm
	rm -f galeb-health-${VERSION}-1.el7.noarch.rpm

dist: galeb-next
	type fpm > /dev/null 2>&1 && \
    for service in router health; do \
        cd $$service/target && \
        cp -a ../../wrapper . && \
        cp ../wrapper.conf wrapper/conf/ && \
        cp -a ../initscript wrapper/bin/ && \
        fpm -s dir \
            -t rpm \
            -n "galeb-$$service" \
            -v ${VERSION} \
            --iteration 1.el7 \
            -a noarch \
            --rpm-os linux \
            --prefix /opt/galeb/$$service/lib \
            -m '<galeb@corp.globo.com>' \
            --vendor 'Globo.com' \
            --description 'Galeb $$service service' \
            -f -p ../../galeb-$$service-${VERSION}-1.el7.noarch.rpm *jar wrapper && \
        cd -; \
    done

doc:
	cd core/docs && rm -rf html && doxygen Doxyfile && \
	cd ../../health/docs && rm -rf html && doxygen Doxyfile && \
	cd ../../router/docs && rm -rf html && doxygen Doxyfile && \
  cd ../..
