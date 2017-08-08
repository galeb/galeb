RPM_VER=4.0.12
VERSION=${RPM_VER}
RELEASE=1

deploy-snapshot:
	mvn clean install -DskipTests deploy:deploy -DaltDeploymentRepository=oss-jfrog::default::http://oss.jfrog.org/artifactory/oss-snapshot-local

galeb-next: clean
	mvn package -DskipTests

test:
	mvn test

clean:
	mvn clean
	rm -f galeb-router-${RPM_VER}-1.el7.noarch.rpm
	rm -f galeb-health-${RPM_VER}-1.el7.noarch.rpm

dist: galeb-next
	type fpm > /dev/null 2>&1 && \
    for service in router health ; do \
        old=$$(pwd) && \
        cd $$service/target && \
        mkdir -p lib conf logs/tmp && \
        cd lib && \
        echo "#version ${VERSION}" > VERSION && \
        git show --summary >> VERSION && \
        cp -av ../../../wrapper . && \
        cp -av ../../../confd/confd-0.11.0-linux-amd64 confd && \
        cp -av ../../confd ../conf/confd || true  && \
        cp -v ../../wrapper.conf ../conf/ && \
        [ -f ../../log4j.xml ] && cp -v ../../log4j.xml ../conf/ || true && \
        cp -av ../../scripts ../ || true  && \
        cp -v ../galeb-$$service-${VERSION}-SNAPSHOT.jar galeb-$$service.jar && \
        cp -av ../../initscript wrapper/bin/ && \
        cd .. && \
        fpm -s dir \
            -t rpm \
            -n "galeb-$$service" \
            -v ${RPM_VER} \
            --iteration ${RELEASE}.el7 \
            -a noarch \
            --rpm-os linux \
            --prefix /opt/galeb/$$service \
            -m '<galeb@corp.globo.com>' \
            --vendor 'Globo.com' \
            --description 'Galeb $$service service' \
            --after-install scripts/postinstall \
            -f -p ../../galeb-$$service-${RPM_VER}-${RELEASE}.el7.noarch.rpm lib conf logs scripts && \
        cd $$old; \
    done

doc:
	cd core/docs && rm -rf html && doxygen Doxyfile && \
	cd ../../health/docs && rm -rf html && doxygen Doxyfile && \
	cd ../../router/docs && rm -rf html && doxygen Doxyfile && \
  cd ../..
